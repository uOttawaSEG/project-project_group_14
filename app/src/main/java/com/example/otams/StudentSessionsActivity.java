package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentSessionsActivity extends AppCompatActivity {

    private RecyclerView rvSessions;
    private TextView tvEmpty;
    private StudentSessionsAdapter adapter;
    private Button btnPastSessions;
    private DatabaseReference sessionsRef;
    private String studentId;

    private List<Session> sessionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_session_list);

        rvSessions = findViewById(R.id.rvSessions);
        tvEmpty = findViewById(R.id.tvEmpty);

        btnPastSessions = findViewById(R.id.btnPastSessions);

        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

        adapter = new StudentSessionsAdapter(this, sessionList);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(adapter);

        loadSessions();
        btnPastSessions.setOnClickListener(v -> {
            Intent intent = new Intent(StudentSessionsActivity.this,
                    StudentPastSessionsActivity.class);
            startActivity(intent);
        });
    }

    private void loadSessions() {
        sessionsRef.orderByChild("studentId").equalTo(studentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sessionList.clear();

                        for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                            Session session = sessionSnap.getValue(Session.class);
                            if (session != null) {
                                session.sessionId = sessionSnap.getKey();
                                sessionList.add(session);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if (sessionList.isEmpty()) {
                            tvEmpty.setVisibility(View.VISIBLE);
                            rvSessions.setVisibility(View.GONE);
                        } else {
                            tvEmpty.setVisibility(View.GONE);
                            rvSessions.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
