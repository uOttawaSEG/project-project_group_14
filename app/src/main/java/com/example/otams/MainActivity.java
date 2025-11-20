package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private MaterialButton loginButton, signUpButton;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("registrationRequests");


        loginButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();



            // Check if it’s the admin Rohachena
            if (email.equalsIgnoreCase("Rohachena@gmail.com") && password.equals("group14")) {
                Intent intent = new Intent(MainActivity.this, WelcomePage.class);
                intent.putExtra("name", "Administrator");
                intent.putExtra("role", "Administrator");
                startActivity(intent);
                finish();
                return;
            }


            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user == null) return;

                            String uid = user.getUid();


                            checkUserStatus(uid);
                        }
                    });
        });

        // SIGN UP BUTTON
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PreSignUpActivity.class);
            startActivity(intent);
        });
    }

    private void checkUserStatus(String uid) {
        // Check first in students
        dbRef.child("students").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot studentSnapshot) {
                if (studentSnapshot.exists()) {
                    handleUserStatus(studentSnapshot);
                } else {
                    // If not student, check tutors
                    dbRef.child("tutors").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot tutorSnapshot) {
                            if (tutorSnapshot.exists()) {
                                handleUserStatus(tutorSnapshot);
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }


                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

    private void handleUserStatus(DataSnapshot snapshot) {
        String status = snapshot.child("status").getValue(String.class);
        String firstName = snapshot.child("firstName").getValue(String.class);
        String role = snapshot.child("role").getValue(String.class);

        if (status == null) status = "pending";

        switch (status.toLowerCase()) {
            case "pending":
                Intent pendingIntent = new Intent(MainActivity.this, PendingActivity.class);
                pendingIntent.putExtra("firstName", firstName);  
                startActivity(pendingIntent);
                finish();
                break;

            case "rejected":
                Intent rejectedIntent = new Intent(MainActivity.this, RejectedActivity.class);
                rejectedIntent.putExtra("name", firstName);
                startActivity(rejectedIntent);
                finish();
                break;

            case "approved":
                Intent approvedIntent = new Intent(MainActivity.this, WelcomePage.class);
                approvedIntent.putExtra("name", firstName);
                approvedIntent.putExtra("role", role);
                startActivity(approvedIntent);
                finish();
                break;


        }
    }
}
