package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StudentDashboardActivity extends AppCompatActivity {

    private Button btnBookSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        btnBookSession = findViewById(R.id.btnBookSession);
        Button btnViewSessions = findViewById(R.id.btnViewSessions);

        // Student Booking screen
        btnBookSession.setOnClickListener(v -> {
            startActivity(new Intent(StudentDashboardActivity.this, BookSessionActivity.class));
        });

        //Student session list screen
        btnViewSessions.setOnClickListener(v -> {
            startActivity(new Intent(StudentDashboardActivity.this, StudentSessionsActivity.class));
        });
    }
}

