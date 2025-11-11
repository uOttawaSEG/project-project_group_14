package com.example.otams;

import android.os.Bundle;
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
    private DatabaseReference sessionsRef;
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
                            if (session != null && isSessionInCategory(session)) {
                                sessionList.add(session);
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
                return sessionDateTime.before(now) || "completed".equals(session.status);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }
}