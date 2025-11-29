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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionsActivity extends AppCompatActivity {

    private RecyclerView rvSessions;
    private TextView tvTitle;
    private DatabaseReference sessionsRef, usersRef;
    private String tutorId;
    private String sessionType;
    private List<Session> sessionList;
    private SessionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        rvSessions = findViewById(R.id.rvSessions);
        tvTitle = findViewById(R.id.tvTitle);

        sessionType = getIntent().getStringExtra("sessionType");
        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        usersRef = FirebaseDatabase.getInstance().getReference("registrationRequests");

        sessionList = new ArrayList<>();
        adapter = new SessionsAdapter(sessionList);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(adapter);

        setupTitle();
        loadSessions();
    }

    private void setupTitle() {
        if ("upcoming".equals(sessionType)) {
            tvTitle.setText("Upcoming Sessions");
        } else if ("past".equals(sessionType)) {
            tvTitle.setText("Past Sessions");
        }
    }

    private void loadSessions() {
        sessionsRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sessionList.clear();
                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            Session session = sessionSnapshot.getValue(Session.class);
                            if (session != null) {
                                session.sessionId = sessionSnapshot.getKey();
                                if (isSessionInCategory(session)) {
                                    sessionList.add(session);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(SessionsActivity.this,
                                "Failed to load sessions: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isSessionInCategory(Session session) {
        if (session.date == null || session.startTime == null) return false;

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            String dateTimeStr = session.date + " " + session.startTime;
            Date sessionDateTime = dateFormat.parse(dateTimeStr);
            Date now = new Date();

            if ("upcoming".equals(sessionType)) {
                return sessionDateTime.after(now) &&
                        ("approved".equals(session.status) || "pending".equals(session.status));
            } else if ("past".equals(sessionType)) {
                return sessionDateTime.before(now) || "completed".equals(session.status) || "cancelled".equals(session.status);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void cancelSession(Session session) {
        DatabaseReference sessionRef = sessionsRef.child(session.sessionId);
        sessionRef.child("status").setValue("cancelled")
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Session cancelled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {
        private List<Session> sessions;

        public SessionsAdapter(List<Session> sessions) {
            this.sessions = sessions;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_session, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Session session = sessions.get(position);

            // Load student information
            loadStudentInfo(session.studentId, holder.tvStudentName);

            holder.tvCourse.setText("Course: " + (session.course != null ? session.course : "N/A"));
            holder.tvDateTime.setText(session.date + " " + session.startTime + " - " + session.endTime);
            holder.tvStatus.setText("Status: " + (session.status != null ? session.status : "Unknown"));


            setupSessionActions(session, holder);
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

        private void setupSessionActions(Session session, ViewHolder holder) {
            // Show cancel button only for upcoming approved sessions
            if ("upcoming".equals(sessionType) && "approved".equals(session.status)) {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setOnClickListener(v -> cancelSession(session));
            } else {
                holder.btnCancel.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStudentName, tvCourse, tvDateTime, tvStatus;
            Button btnCancel;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvStudentName = itemView.findViewById(R.id.tvStudentName);
                tvCourse = itemView.findViewById(R.id.tvCourse);
                tvDateTime = itemView.findViewById(R.id.tvDateTime);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnCancel = itemView.findViewById(R.id.btnCancel);
            }
        }
    }
}
