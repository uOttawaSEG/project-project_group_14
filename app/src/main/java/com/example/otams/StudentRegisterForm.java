package com.example.otams;

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

public class StudentRegisterForm extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, passwordInput, phoneInput, programInput;
    private MaterialButton registerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference requestsRef;

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

        mAuth = FirebaseAuth.getInstance();
        requestsRef = FirebaseDatabase.getInstance().getReference("registrationRequests").child("students");

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!validateInputs()) return;

                String firstName = firstNameInput.getText().toString().trim();
                String lastName = lastNameInput.getText().toString().trim();
                String email = emailInput.getText().toString().trim();
                String password = passwordInput.getText().toString().trim();
                String phone = phoneInput.getText().toString().trim();
                String program = programInput.getText().toString().trim();

                registerButton.setEnabled(false);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(StudentRegisterForm.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String uid = mAuth.getCurrentUser().getUid();

                                    RegistrationRequest req = new RegistrationRequest();
                                    req.firstName = firstName;
                                    req.lastName = lastName;
                                    req.email = email;
                                    req.phone = phone;
                                    req.program = program;
                                    req.status = "pending";
                                    req.role = "student";

                                    requestsRef.child(uid).setValue(req)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> dbTask) {
                                                    registerButton.setEnabled(true);
                                                    if (dbTask.isSuccessful()) {
                                                        mAuth.signOut();
                                                        Toast.makeText(StudentRegisterForm.this,
                                                                "Registration submitted! Wait for admin approval.",
                                                                Toast.LENGTH_LONG).show();
                                                        finish();
                                                    }
                                                }
                                            });

                                }
                            }
                        });
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (TextUtils.isEmpty(firstNameInput.getText().toString().trim())) {
            firstNameInput.setError("First name is required");
            isValid = false;
        }
        if (TextUtils.isEmpty(lastNameInput.getText().toString().trim())) {
            lastNameInput.setError("Last name is required");
            isValid = false;
        }
        String email = emailInput.getText().toString().trim();
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            isValid = false;
        }
        String password = passwordInput.getText().toString().trim();
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            isValid = false;
        }
        String phone = phoneInput.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || !Patterns.PHONE.matcher(phone).matches() || phone.length() < 7) {
            phoneInput.setError("Enter a valid phone number");
            isValid = false;
        }
        if (TextUtils.isEmpty(programInput.getText().toString().trim())) {
            programInput.setError("Program of study is required");
            isValid = false;
        }

        return isValid;
    }
}



