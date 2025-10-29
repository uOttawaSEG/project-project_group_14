package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class StudentRegisterForm extends AppCompatActivity {
    private EditText firstNameInput, lastNameInput, emailInput, passwordInput, phoneInput, programInput;
    private MaterialButton registerButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference requestsRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.studentregister);

        // UI refs
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        phoneInput = findViewById(R.id.phoneInput);
        programInput = findViewById(R.id.programInput);
        registerButton = findViewById(R.id.registerButton);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        // We'll store requests under /registrationRequests/students/{uid}
        requestsRootRef = FirebaseDatabase.getInstance().getReference("registrationRequests").child("students");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateInputs()) {
                    // Disable button to prevent double clicks
                    registerButton.setEnabled(false);

                    final String firstName = firstNameInput.getText().toString().trim();
                    final String lastName = lastNameInput.getText().toString().trim();
                    final String email = emailInput.getText().toString().trim();
                    final String password = passwordInput.getText().toString().trim();
                    final String phone = phoneInput.getText().toString().trim();
                    final String program = programInput.getText().toString().trim();

                    // Create auth user (we create auth so email uniqueness is enforced).
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(StudentRegisterForm.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> authTask) {
                                    if (authTask.isSuccessful()) {
                                        String uid = mAuth.getCurrentUser().getUid();

                                        // Build request map (do NOT include password)
                                        Map<String, Object> requestMap = new HashMap<>();
                                        requestMap.put("uid", uid);
                                        requestMap.put("firstName", firstName);
                                        requestMap.put("lastName", lastName);
                                        requestMap.put("email", email);
                                        requestMap.put("phone", phone);
                                        requestMap.put("program", program);
                                        requestMap.put("role", "student");
                                        requestMap.put("status", "pending");
                                        requestMap.put("createdAt", ServerValue.TIMESTAMP);

                                        // Save under /registrationRequests/students/{uid}
                                        requestsRootRef.child(uid).setValue(requestMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> dbTask) {
                                                        registerButton.setEnabled(true);
                                                        if (dbTask.isSuccessful()) {
                                                            // Sign out so user cannot access app until admin approves
                                                            mAuth.signOut();

                                                            Toast.makeText(StudentRegisterForm.this,
                                                                    "Registration submitted. Await admin approval.",
                                                                    Toast.LENGTH_LONG).show();

                                                            // Optionally navigate back to login screen
                                                            finish();

                                                        } else {
                                                            // DB write failed — show message and delete the created auth user to avoid orphaned accounts
                                                            String err = dbTask.getException() != null ? dbTask.getException().getMessage() : "Unknown error";
                                                            Toast.makeText(StudentRegisterForm.this,
                                                                    "Failed to submit registration: " + err,
                                                                    Toast.LENGTH_LONG).show();

                                                            // Try to delete the auth user we just created (cleanup)
                                                            if (mAuth.getCurrentUser() != null) {
                                                                mAuth.getCurrentUser().delete();
                                                            }
                                                        }
                                                    }
                                                });

                                    } else {
                                        registerButton.setEnabled(true);
                                        String err = authTask.getException() != null ? authTask.getException().getMessage() : "Registration failed";
                                        Toast.makeText(StudentRegisterForm.this,
                                                "Registration failed: " + err,
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
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


