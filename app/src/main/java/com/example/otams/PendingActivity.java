package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;

public class PendingActivity extends AppCompatActivity {

    private TextView pendingMessage;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending);

        // Handle system UI padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pendingLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pendingMessage = findViewById(R.id.pendingMessage);
        logoutButton = findViewById(R.id.logoutButton);


        String firstName = getIntent().getStringExtra("firstName");


        if (firstName != null && !firstName.isEmpty()) {
            pendingMessage.setText("Hi " + firstName + ", your registration request is pending approval.\n\nPlease check back later once the administrator has reviewed your request.");
        } else {
            pendingMessage.setText("Hi, your registration request is pending approval.\n\nPlease check back later once the administrator has reviewed your request.");
        }

        logoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(PendingActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}

