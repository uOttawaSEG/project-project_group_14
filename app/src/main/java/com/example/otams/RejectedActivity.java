package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RejectedActivity extends AppCompatActivity {

    private TextView rejectedMessage;
    private Button backToLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rejectedLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        rejectedMessage = findViewById(R.id.rejectedMessage);
        backToLoginButton = findViewById(R.id.backToLoginButton);


        String name = getIntent().getStringExtra("name");


        rejectedMessage.setText("Your registration request was rejected.\n\nPlease contact the administrator at 555-123-4567.");


        backToLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RejectedActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
