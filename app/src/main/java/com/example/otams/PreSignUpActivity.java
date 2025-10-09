package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class PreSignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_sign_up);

        MaterialButton btnStudent = findViewById(R.id.btnStudent);
        MaterialButton btnTutor = findViewById(R.id.btnTutor);

        btnStudent.setOnClickListener(v -> {
            Intent intent = new Intent(PreSignUpActivity.this, com.example.otams.StudentRegisterForm.class);
            startActivity(intent);
        });

        btnTutor.setOnClickListener(v -> {
            Intent intent = new Intent(PreSignUpActivity.this, com.example.otams.TutorRegisterForm.class);
            startActivity(intent);
        });
    }
}

