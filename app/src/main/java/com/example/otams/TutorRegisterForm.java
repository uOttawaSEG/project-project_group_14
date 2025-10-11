package com.example.otams;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class TutorRegisterForm extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, phoneInput, passwordInput, degreeInput, coursesInput;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorregister);


        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        degreeInput = findViewById(R.id.degreeInput);
        coursesInput = findViewById(R.id.coursesInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    String firstName = firstNameInput.getText().toString().trim();
                    String lastName = lastNameInput.getText().toString().trim();
                    String fullName = firstName + " " + lastName;

                    Toast.makeText(TutorRegisterForm.this,
                            "Tutor Registration Successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to WelcomeActivity
                    Intent intent = new Intent(TutorRegisterForm.this, WelcomePage.class);
                    intent.putExtra("name", fullName);
                    intent.putExtra("role", "Tutor");
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private boolean validateInputs() {

        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String degree = degreeInput.getText().toString().trim();
        String courses = coursesInput.getText().toString().trim();

        boolean isValid = true;

        if (TextUtils.isEmpty(firstName)) {
            firstNameInput.setError("First name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameInput.setError("Last name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            isValid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches()) {
            phoneInput.setError("Enter a valid phone number");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid = false;
        }

        if (TextUtils.isEmpty(degree)) {
            degreeInput.setError("Highest degree is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(courses)) {
            coursesInput.setError("Please list at least one course you can tutor");
            isValid = false;
        } else {
            String[] courseList = courses.split(",");
            if (courseList.length == 0 || courses.trim().isEmpty()) {
                coursesInput.setError("Please list at least one course you can tutor");
                isValid = false;
            }
        }

        return isValid;
    }
}

