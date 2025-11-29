package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class BookSessionActivity extends AppCompatActivity {

    private EditText etCourseSearch;
    private Button btnSearch;
    private RecyclerView rvAvailableSlots;
    private DatabaseReference slotsRef, sessionsRef, tutorsRef;
    private String studentId;
    private List<AvailabilitySlot> availableSlots;
    private AvailableSlotsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_session);

        etCourseSearch = findViewById(R.id.etCourseSearch);
        btnSearch = findViewById(R.id.btnSearch);
        rvAvailableSlots = findViewById(R.id.rvAvailableSlots);

        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        slotsRef = FirebaseDatabase.getInstance().getReference("availabilitySlots");
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        tutorsRef = FirebaseDatabase.getInstance().getReference("sessions");

        tutorsRef = FirebaseDatabase.getInstance()
                .getReference("registrationRequests")
                .child("tutors");

        availableSlots = new ArrayList<>();
        adapter = new AvailableSlotsAdapter(availableSlots, this::bookSession);
        rvAvailableSlots.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableSlots.setAdapter(adapter);

        btnSearch.setOnClickListener(v -> searchAvailableSlots());
    }

    private void searchAvailableSlots() {
        String course = etCourseSearch.getText().toString().trim();
        if (course.isEmpty()) {
            Toast.makeText(this, "Please enter a course code", Toast.LENGTH_SHORT).show();
            return;
        }

        availableSlots.clear();

        slotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    AvailabilitySlot slot = slotSnapshot.getValue(AvailabilitySlot.class);
                    if (slot != null) {
                        slot.slotId = slotSnapshot.getKey();

                        checkTutorCourses(slot, course, () -> {
                            checkSlotAvailability(slot, () -> {
                                availableSlots.add(slot);
                                adapter.notifyDataSetChanged();
                            });
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BookSessionActivity.this,
                        "Error searching slots: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkTutorCourses(AvailabilitySlot slot, String course, Runnable onMatch) {
        tutorsRef.child(slot.tutorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String tutorCourses = snapshot.child("courses").getValue(String.class);
                            if (tutorCourses != null && tutorCourses.toLowerCase().contains(course.toLowerCase())) {
                                onMatch.run();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkSlotAvailability(AvailabilitySlot slot, Runnable onAvailable) {
        sessionsRef.orderByChild("slotId").equalTo(slot.slotId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isBooked = false;
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            Session session = sessionSnapshot.getValue(Session.class);
                            if (session != null &&
                                    ("pending".equals(session.status) || "approved".equals(session.status))) {
                                isBooked = true;
                                break;
                            }
                        }

                        if (!isBooked) {
                            onAvailable.run();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void bookSession(AvailabilitySlot slot, String course) {

        sessionsRef.orderByChild("studentId").equalTo(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean hasConflict = false;

                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            Session existingSession = sessionSnapshot.getValue(Session.class);
                            if (existingSession != null &&
                                    existingSession.date.equals(slot.date) &&
                                    isTimeOverlapping(slot.startTime, slot.endTime,
                                            existingSession.startTime, existingSession.endTime) &&
                                    ("pending".equals(existingSession.status) || "approved".equals(existingSession.status))) {
                                hasConflict = true;
                                break;
                            }
                        }

                        if (hasConflict) {
                            Toast.makeText(BookSessionActivity.this,
                                    "Time conflict with existing session", Toast.LENGTH_SHORT).show();
                        } else {
                            createSessionRequest(slot, course);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(BookSessionActivity.this,
                                "Error checking conflicts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isTimeOverlapping(String start1, String end1, String start2, String end2) {
        return (start1.compareTo(end2) < 0 && end1.compareTo(start2) > 0);
    }

    private void createSessionRequest(AvailabilitySlot slot, String course) {
        Session session = new Session();
        String sessionId = sessionsRef.push().getKey();
        session.sessionId = sessionId;
        session.studentId = studentId;
        session.tutorId = slot.tutorId;
        session.course = course;
        session.date = slot.date;
        session.startTime = slot.startTime;
        session.endTime = slot.endTime;
        session.createdAt = System.currentTimeMillis();
        session.updatedAt = System.currentTimeMillis();
        session.status = slot.autoApprove ? "approved" : "pending";

        sessionsRef.child(sessionId).setValue(session)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(BookSessionActivity.this,
                            "Session request " + (slot.autoApprove ? "approved" : "submitted"),
                            Toast.LENGTH_SHORT).show();
                    availableSlots.remove(slot);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(BookSessionActivity.this,
                            "Failed to book session: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    interface BookSessionListener {
        void onBookSession(AvailabilitySlot slot, String course);
    }

    private class AvailableSlotsAdapter extends RecyclerView.Adapter<AvailableSlotsAdapter.ViewHolder> {
        private List<AvailabilitySlot> slots;
        private BookSessionListener listener;

        public AvailableSlotsAdapter(List<AvailabilitySlot> slots, BookSessionListener listener) {
            this.slots = slots;
            this.listener = listener;
        }

        @NonNull
        @Override
        public AvailableSlotsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AvailableSlotsAdapter.ViewHolder holder, int position) {
            AvailabilitySlot slot = slots.get(position);

            holder.tvDateTime.setText(slot.date + " " + slot.startTime + " - " + slot.endTime);

            loadTutorInfo(slot.tutorId, holder);

            holder.btnBook.setOnClickListener(v -> {
                String course = etCourseSearch.getText().toString().trim();
                if (!course.isEmpty()) {
                    listener.onBookSession(slot, course);
                }
            });
        }

        private void loadTutorInfo(String tutorId, final ViewHolder holder) {
            // load tutor name
            tutorsRef.child(tutorId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                String tutorName = (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
                                holder.tvTutorName.setText("Tutor: " + tutorName.trim());
                            } else {
                                holder.tvTutorName.setText("Tutor: Unknown");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            holder.tvTutorName.setText("Tutor: Error loading");
                        }
                    });

            // compute average rating for tutor
            sessionsRef.orderByChild("tutorId").equalTo(tutorId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snap) {
                            int total = 0;
                            int count = 0;
                            for (DataSnapshot s : snap.getChildren()) {
                                Integer r = s.child("rating").getValue(Integer.class);
                                if (r != null && r > 0) {
                                    total += r;
                                    count++;
                                }
                            }
                            float avg = (count == 0) ? 0f : (float) total / count;
                            holder.ratingBarTutor.setRating(avg);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) { }

                    });
        }

        @Override
        public int getItemCount() {
            return slots == null ? 0 : slots.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTutorName, tvDateTime;
            Button btnBook;
            RatingBar ratingBarTutor;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTutorName = itemView.findViewById(R.id.tvTutorName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                btnBook = itemView.findViewById(R.id.btnBook);
                ratingBarTutor = itemView.findViewById(R.id.ratingBarTutor); // <-- added
            }
        }
    }
}
