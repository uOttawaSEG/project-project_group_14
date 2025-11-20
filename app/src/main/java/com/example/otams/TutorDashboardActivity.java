package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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
            try {
                startActivity(new Intent(TutorDashboardActivity.this, UpcomingSessionsActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Unable to open Upcoming Sessions", Toast.LENGTH_SHORT).show();
            }
        });




        btnPastSessions.setOnClickListener(v -> {
            startActivity(new Intent(TutorDashboardActivity.this, PastSessionsActivity.class));
        });


        btnPendingRequests.setOnClickListener(v -> {
            Intent intent = new Intent(TutorDashboardActivity.this, PendingRequestsActivity.class);
            startActivity(intent);
        });
    }
}
