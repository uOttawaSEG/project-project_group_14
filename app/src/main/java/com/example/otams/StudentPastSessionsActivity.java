package com.example.otams;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentPastSessionsActivity extends AppCompatActivity {

    private RecyclerView rvPast;
    private TextView tvEmptyPast;
    private StudentPastSessionsAdapter adapter;
    private List<Session> pastList = new ArrayList<>();
    private String studentId;
    private DatabaseReference sessionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_past_sessions);

        rvPast = findViewById(R.id.rvPastSessions);
        tvEmptyPast = findViewById(R.id.tvEmptyPast);

        studentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

        adapter = new StudentPastSessionsAdapter(this, pastList);
        rvPast.setLayoutManager(new LinearLayoutManager(this));
        rvPast.setAdapter(adapter);

        loadPastSessions();
    }

    private void loadPastSessions() {
        sessionsRef.orderByChild("studentId").equalTo(studentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pastList.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Session s = snap.getValue(Session.class);
                            if (s != null && "approved".equalsIgnoreCase(s.status)) {
                                try {
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    Date sessionEnd = sdf.parse((s.date != null ? s.date : "") + " " + (s.endTime != null ? s.endTime : ""));

                                    if (sessionEnd != null && sessionEnd.getTime() < System.currentTimeMillis()) {
                                        s.sessionId = snap.getKey();
                                        pastList.add(s);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        tvEmptyPast.setVisibility(pastList.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}


