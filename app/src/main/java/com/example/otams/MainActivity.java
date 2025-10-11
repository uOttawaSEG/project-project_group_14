package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {
    EditText emailInput, passwordInput;
    MaterialButton loginButton, signUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.EmailText);
        passwordInput = findViewById(R.id.PasswordText);
        loginButton = findViewById(R.id.LoginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.equals("Rohachena@gmail.com") && password.equals("group14")) {
                Intent intent = new Intent(MainActivity.this, WelcomePage.class);
                intent.putExtra("name", "Administrator");
                intent.putExtra("role", "Administrator");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialButton signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PreSignUpActivity.class);
            startActivity(intent);
        });
    }
}
