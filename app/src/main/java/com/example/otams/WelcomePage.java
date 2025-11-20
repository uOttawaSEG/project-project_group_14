package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class WelcomePage extends AppCompatActivity {

    private TextView welcomeTitle, userNameText, userRoleText;
    private MaterialButton logoutButton;
    private MaterialButton adminInboxButton;
    private MaterialButton tutorDashboardButton;
    private MaterialButton studentDashboardButton;

    private String name, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeTitle = findViewById(R.id.welcomeTitle);
        userNameText = findViewById(R.id.userNameText);
        userRoleText = findViewById(R.id.userRoleText);
        logoutButton = findViewById(R.id.LogoutButton);
        adminInboxButton = findViewById(R.id.adminInboxButton);
        tutorDashboardButton = findViewById(R.id.tutorDashboardButton);
        studentDashboardButton = findViewById(R.id.studentDashboardButton);

        name = getIntent().getStringExtra("name");
        role = getIntent().getStringExtra("role");

        if (name == null) name = "User";
        if (role == null) role = "User";

        welcomeTitle.setText("Welcome, " + name + "!");
        userNameText.setText(name);
        userRoleText.setText("You are logged in as " + role);

        String roleNormalized = role.trim();



        if ("Administrator".equalsIgnoreCase(roleNormalized)) {
            adminInboxButton.setVisibility(View.VISIBLE);
            adminInboxButton.setOnClickListener(v -> {

                Intent i = new Intent(WelcomePage.this, AdminInboxActivity.class);
                startActivity(i);
            });
        } else {
            adminInboxButton.setVisibility(View.GONE);
        }

        if ("tutor".equalsIgnoreCase(roleNormalized)) {
            tutorDashboardButton.setVisibility(View.VISIBLE);
            tutorDashboardButton.setOnClickListener(v -> {
                Intent i = new Intent(WelcomePage.this, TutorDashboardActivity.class);
                startActivity(i);
            });
        } else {
            tutorDashboardButton.setVisibility(View.GONE);
        }


        if ("student".equalsIgnoreCase(roleNormalized)) {
            studentDashboardButton.setVisibility(View.VISIBLE);
            studentDashboardButton.setOnClickListener(v -> {
                Intent i = new Intent(WelcomePage.this, StudentDashboardActivity.class);
                startActivity(i);
            });
        } else {
            studentDashboardButton.setVisibility(View.GONE);
        }

        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(WelcomePage.this, LogoutActivity.class);
            logoutIntent.putExtra("name", name);
            logoutIntent.putExtra("role", role);
            startActivity(logoutIntent);
        });
    }
}





