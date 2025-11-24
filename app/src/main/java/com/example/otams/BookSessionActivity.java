package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        tutorsRef = FirebaseDatabase.getInstance().getReference("registrationRequests");

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

        // Get all availability slots
        slotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                    AvailabilitySlot slot = slotSnapshot.getValue(AvailabilitySlot.class);
                    if (slot != null) {
                        slot.slotId = slotSnapshot.getKey();

                        // Check if tutor offers this course
                        checkTutorCourses(slot, course, () -> {
                            // Check if slot is already booked
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
        tutorsRef.child("tutors").child(slot.tutorId)
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
        session.sessionId = sessionId;              // ⭐ ADDED
        session.studentId = studentId;              // ⭐ ADDED
        session.tutorId = slot.tutorId;             // ⭐ ADDED
        session.course = course;                    // ⭐ ADDED
        session.date = slot.date;                   // ⭐ ADDED
        session.startTime = slot.startTime;         // ⭐ ADDED
        session.endTime = slot.endTime;             // ⭐ ADDED
        session.createdAt = System.currentTimeMillis(); // ⭐ ADDED
        session.updatedAt = System.currentTimeMillis(); // ⭐ ADDED
        session.status = slot.autoApprove ? "approved" : "pending"; // ⭐ UPDATED

        //// ⭐ CHANGED — removed .toMap() because it's not needed anymore
        sessionsRef.child(sessionId).setValue(session)  // ⭐ UPDATED
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
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_available_slot, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        private void loadTutorInfo(String tutorId, ViewHolder holder) {
            tutorsRef.child("tutors").child(tutorId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                String tutorName = (firstName != null ? firstName : "") + " " +
                                        (lastName != null ? lastName : "");
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
        }

        @Override
        public int getItemCount() {
            return slots.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTutorName, tvDateTime;
            Button btnBook;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTutorName = itemView.findViewById(R.id.tvTutorName);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                btnBook = itemView.findViewById(R.id.btnBook);
            }
        }
    }
}
