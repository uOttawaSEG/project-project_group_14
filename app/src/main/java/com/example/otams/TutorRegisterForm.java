package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

public class TutorRegisterForm extends AppCompatActivity {

    private static final String TAG = "TutorRegister";

    private EditText firstNameInput, lastNameInput, emailInput, phoneInput, passwordInput, degreeInput, coursesInput;
    private MaterialButton registerButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference requestsRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorregister);

        // UI refs
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        passwordInput = findViewById(R.id.passwordInput);
        degreeInput = findViewById(R.id.degreeInput);
        coursesInput = findViewById(R.id.coursesInput);
        registerButton = findViewById(R.id.registerButton);

        // Firebase
        mAuth = FirebaseAuth.getInstance();
        requestsRootRef = FirebaseDatabase.getInstance().getReference("registrationRequests").child("tutors");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleRegisterClick();
            }
        });
    }

    private void handleRegisterClick() {
        if (!validateInputs()) return;

        final String firstName = firstNameInput.getText().toString().trim();
        final String lastName = lastNameInput.getText().toString().trim();
        final String email = emailInput.getText().toString().trim();
        final String password = passwordInput.getText().toString().trim();
        final String phone = phoneInput.getText().toString().trim();
        final String degree = degreeInput.getText().toString().trim();
        final String courses = coursesInput.getText().toString().trim();

        // disable button to avoid duplicate submissions
        registerButton.setEnabled(false);

        // 1) create auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(TutorRegisterForm.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> authTask) {
                        if (authTask.isSuccessful()) {
                            String uid = mAuth.getCurrentUser().getUid();

                            // 2) build request map (no password)
                            Map<String, Object> requestMap = new HashMap<>();
                            requestMap.put("uid", uid);
                            requestMap.put("firstName", firstName);
                            requestMap.put("lastName", lastName);
                            requestMap.put("email", email);
                            requestMap.put("phone", phone);
                            requestMap.put("degree", degree);
                            requestMap.put("courses", courses);
                            requestMap.put("role", "tutor");
                            requestMap.put("status", "pending");
                            requestMap.put("createdAt", ServerValue.TIMESTAMP);

                            // 3) write to Realtime DB at /registrationRequests/tutors/{uid}
                            requestsRootRef.child(uid).setValue(requestMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> dbTask) {
                                            registerButton.setEnabled(true);
                                            if (dbTask.isSuccessful()) {
                                                // successful submission - sign out so user can't access app until admin approves
                                                mAuth.signOut();

                                                Toast.makeText(TutorRegisterForm.this,
                                                        "Registration submitted. Await admin approval.",
                                                        Toast.LENGTH_LONG).show();

                                                // finish or navigate to login
                                                finish();
                                            } else {
                                                Exception dbEx = dbTask.getException();
                                                String msg = dbEx == null ? "DB write failed (no details)" : dbEx.getMessage();
                                                Log.e(TAG, "DB write failed", dbEx);
                                                Toast.makeText(TutorRegisterForm.this, "Registration failed (DB): " + msg, Toast.LENGTH_LONG).show();

                                                // Clean up created Auth user (best effort)
                                                if (mAuth.getCurrentUser() != null) {
                                                    mAuth.getCurrentUser().delete().addOnCompleteListener(delTask -> {
                                                        if (!delTask.isSuccessful()) {
                                                            Log.e(TAG, "Failed to delete orphaned auth user", delTask.getException());
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });

                        } else {
                            registerButton.setEnabled(true);
                            Exception ex = authTask.getException();
                            String msg = ex == null ? "Auth failed (no details)" : ex.getMessage();
                            Log.e(TAG, "createUser failed", ex);
                            Toast.makeText(TutorRegisterForm.this, "Registration failed (Auth): " + msg, Toast.LENGTH_LONG).show();
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


