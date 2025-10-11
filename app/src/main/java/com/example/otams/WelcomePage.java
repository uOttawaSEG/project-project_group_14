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

    private String name, role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        welcomeTitle = findViewById(R.id.welcomeTitle);
        userNameText = findViewById(R.id.userNameText);
        userRoleText = findViewById(R.id.userRoleText);
        logoutButton = findViewById(R.id.LogoutButton);


        name = getIntent().getStringExtra("name");
        role = getIntent().getStringExtra("role");


            welcomeTitle.setText("Welcome, " + name + "!" );



            userRoleText.setText("You are logged in as " + role);


       
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logoutIntent = new Intent(WelcomePage.this, LogoutActivity.class);
                logoutIntent.putExtra("name", name);
                logoutIntent.putExtra("role", role);
                startActivity(logoutIntent);
            }
        });
    }
}



