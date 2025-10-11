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
public class StudentRegisterForm extends AppCompatActivity{
    private EditText firstNameInput, lastNameInput, emailInput, passwordInput, phoneInput, programInput;
    private MaterialButton registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studentregister);

        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        programInput = findViewById(R.id.programInput);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    String firstName = firstNameInput.getText().toString().trim();
                    String lastName = lastNameInput.getText().toString().trim();
                    String fullName = firstName + " " + lastName;

                    Toast.makeText(StudentRegisterForm.this,
                            "Registration Successful!", Toast.LENGTH_SHORT).show();


                    Intent intent = new Intent(StudentRegisterForm.this, WelcomePage.class);
                    intent.putExtra("name", fullName);
                    intent.putExtra("role", "Student");
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    private boolean validateInputs() {
        boolean valid = true;


        if (TextUtils.isEmpty(firstNameInput.getText().toString().trim())) {
            firstNameInput.setError("First name is required");
            valid = false;
        }


        if (TextUtils.isEmpty(lastNameInput.getText().toString().trim())) {
            lastNameInput.setError("Last name is required");
            valid = false;
        }


        String email = emailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            valid = false;
            }


        String password = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            valid = false;
        } else if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            valid = false;

        }


        String phone = phoneInput.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Phone number is required");
            valid = false;
        } else if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 7) {
            phoneInput.setError("Enter a valid phone number");
            valid = false;
        }
        if (TextUtils.isEmpty(programInput.getText().toString().trim())) {
            programInput.setError("Program of study is required");
            valid = false;
        }

        return valid;
    }
}

