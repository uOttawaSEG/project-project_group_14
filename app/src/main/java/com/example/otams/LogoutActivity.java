package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class LogoutActivity extends AppCompatActivity {

    private TextView logoutMessage;
    private MaterialButton cancelButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);


        logoutMessage = findViewById(R.id.LogoutMessage);
        cancelButton = findViewById(R.id.CancelButton);
        logoutButton = findViewById(R.id.LogoutButton) ;


        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String role = intent.getStringExtra("role");


        logoutMessage.setText("Are you sure you want to log out, " + name + " (" + role + ")?");


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to WelcomeActivity
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(LogoutActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });
    }
}
