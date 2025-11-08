package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class TutorDashboardActivity extends AppCompatActivity {

    private Button btnManageAvailability, btnUpcomingSessions, btnPastSessions, btnPendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_dashboard);

        btnManageAvailability = findViewById(R.id.btnManageAvailability);
        btnUpcomingSessions = findViewById(R.id.btnUpcomingSessions);
        btnPastSessions = findViewById(R.id.btnPastSessions);
        btnPendingRequests = findViewById(R.id.btnPendingRequests);

        btnManageAvailability.setOnClickListener(v -> {
            startActivity(new Intent(TutorDashboardActivity.this, ManageAvailabilityActivity.class));
        });

        btnUpcomingSessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, SessionsActivity.class);
            intent.putExtra("sessionType", "upcoming");
            startActivity(intent);
        });

        btnPastSessions.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, SessionsActivity.class);
            intent.putExtra("sessionType", "past");
            startActivity(intent);
        });

        btnPendingRequests.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, PendingRequestsActivity.class);
            startActivity(intent);
        });
    }
}
