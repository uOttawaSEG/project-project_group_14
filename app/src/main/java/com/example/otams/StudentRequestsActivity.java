package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class StudentRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private DatabaseReference sessionsRef, tutorsRef;
    private String studentId;
    private List<Session> requestList;
    private StudentRequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_requests);

        rvRequests = findViewById(R.id.rvRequests);
        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        tutorsRef = FirebaseDatabase.getInstance().getReference("registrationRequests");

        requestList = new ArrayList<>();
        adapter = new StudentRequestsAdapter(requestList);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        sessionsRef.orderByChild("studentId").equalTo(studentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        requestList.clear();
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            Session session = sessionSnapshot.getValue(Session.class);
                            if (session != null && "pending".equals(session.status)) {
                                session.sessionId = sessionSnapshot.getKey();
                                requestList.add(session);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentRequestsActivity.this,
                                "Failed to load requests: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class StudentRequestsAdapter extends RecyclerView.Adapter<StudentRequestsAdapter.ViewHolder> {
        private List<Session> requests;

        public StudentRequestsAdapter(List<Session> requests) {
            this.requests = requests;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_student_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Session session = requests.get(position);

            // Load tutor information
            loadTutorInfo(session.tutorId, holder.tvTutorName);

            holder.tvCourse.setText("Course: " + (session.course != null ? session.course : "N/A"));
            holder.tvDateTime.setText(session.date + " " + session.startTime + " - " + session.endTime);
            holder.tvStatus.setText("Status: " + (session.status != null ? session.status : "Pending"));
        }

        private void loadTutorInfo(String tutorId, TextView tvTutorName) {
            tutorsRef.child("tutors").child(tutorId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                String tutorName = (firstName != null ? firstName : "") + " " +
                                        (lastName != null ? lastName : "");
                                tvTutorName.setText("Tutor: " + tutorName.trim());
                            } else {
                                tvTutorName.setText("Tutor: Unknown");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvTutorName.setText("Tutor: Error loading");
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return requests.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTutorName, tvCourse, tvDateTime, tvStatus;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTutorName = itemView.findViewById(R.id.tvTutorName);
                tvCourse = itemView.findViewById(R.id.tvCourse);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
            }
        }
    }
}
