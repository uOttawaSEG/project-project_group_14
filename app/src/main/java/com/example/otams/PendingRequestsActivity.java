package com.example.otams;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class PendingRequestsActivity extends AppCompatActivity {

    private RecyclerView rvPendingRequests;
    private DatabaseReference sessionsRef, usersRef;
    private String tutorId;
    private List<Session> pendingSessions;
    private PendingRequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_requests);

        rvPendingRequests = findViewById(R.id.rvPendingRequests);
        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        usersRef = FirebaseDatabase.getInstance().getReference("registrationRequests");

        pendingSessions = new ArrayList<>();
        adapter = new PendingRequestsAdapter(pendingSessions, this::handleSessionAction);
        rvPendingRequests.setLayoutManager(new LinearLayoutManager(this));
        rvPendingRequests.setAdapter(adapter);

        loadPendingRequests();
    }

    private void loadPendingRequests() {
        sessionsRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pendingSessions.clear();
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            Session session = sessionSnapshot.getValue(Session.class);
                            if (session != null && "pending".equals(session.status)) {
                                pendingSessions.add(session);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PendingRequestsActivity.this,
                                "Failed to load requests: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleSessionAction(Session session, String action) {
        DatabaseReference sessionRef = sessionsRef.child(session.sessionId);

        if ("approve".equals(action)) {
            sessionRef.child("status").setValue("approved")
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Session approved", Toast.LENGTH_SHORT).show());

        } else if ("reject".equals(action)) {
            sessionRef.child("status").setValue("rejected")
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this, "Session rejected", Toast.LENGTH_SHORT).show());

        }
    }


    interface SessionActionListener {
        void onSessionAction(Session session, String action);
    }

    private class PendingRequestsAdapter extends RecyclerView.Adapter<PendingRequestsAdapter.ViewHolder> {
        private List<Session> sessions;
        private SessionActionListener listener;

        public PendingRequestsAdapter(List<Session> sessions, SessionActionListener listener) {
            this.sessions = sessions;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_request, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Session session = sessions.get(position);

            holder.tvCourse.setText("Course: " + (session.course != null ? session.course : "N/A"));
            holder.tvDateTime.setText("Date: " + session.date + " " + session.startTime);


            loadStudentInfo(session.studentId, holder.tvStudentName);

            holder.btnApprove.setOnClickListener(v -> listener.onSessionAction(session, "approve"));
            holder.btnReject.setOnClickListener(v -> listener.onSessionAction(session, "reject"));
        }

        private void loadStudentInfo(String studentId, TextView tvStudentName) {
            usersRef.child("students").child(studentId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String firstName = snapshot.child("firstName").getValue(String.class);
                                String lastName = snapshot.child("lastName").getValue(String.class);
                                String studentName = (firstName != null ? firstName : "") + " " +
                                        (lastName != null ? lastName : "");
                                tvStudentName.setText("Student: " + studentName.trim());
                            } else {
                                tvStudentName.setText("Student: Unknown");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            tvStudentName.setText("Student: Error loading");
                        }
                    });
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStudentName, tvCourse, tvDateTime;
            Button btnApprove, btnReject;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStudentName = itemView.findViewById(R.id.tvStudentName);
                tvCourse = itemView.findViewById(R.id.tvCourse);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }
}
