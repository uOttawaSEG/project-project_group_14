package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {

    private Button btnBookSession, btnUpcomingSessions, btnPastSessions, btnMyRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        btnBookSession = findViewById(R.id.btnBookSession);
        btnUpcomingSessions = findViewById(R.id.btnUpcomingSessions);
        btnPastSessions = findViewById(R.id.btnPastSessions);
        btnMyRequests = findViewById(R.id.btnMyRequests);

        btnBookSession.setOnClickListener(v -> {
            startActivity(new Intent(StudentDashboardActivity.this, BookSessionActivity.class));
        });

        btnUpcomingSessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentSessionsActivity.class);
            intent.putExtra("sessionType", "upcoming");
            startActivity(intent);
        });

        btnPastSessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentSessionsActivity.class);
            intent.putExtra("sessionType", "past");
            startActivity(intent);
        });

        btnMyRequests.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, StudentRequestsActivity.class);
            startActivity(intent);
        });
    }
}
