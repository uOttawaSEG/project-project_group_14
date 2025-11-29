package com.example.otams;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

public class UpcomingSessionsActivity extends AppCompatActivity {
    private static final String TAG = "UpcomingSessionsActivity";

    private RecyclerView rvSessions;
    private TextView tvEmpty;
    private SessionsAdapter adapter;
    private final List<Session> sessionList = new ArrayList<>();

    private DatabaseReference sessionsRef;
    private String tutorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_upcoming_sessions);

            rvSessions = findViewById(R.id.rvSessions);
            tvEmpty = findViewById(R.id.tvEmpty);

            rvSessions.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SessionsAdapter(this, sessionList); // matches your constructor
            rvSessions.setAdapter(adapter);

            sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "No signed-in user found. Please sign-in first.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            loadApprovedUpcomingSessions();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onCreate", e);
            Toast.makeText(this, "Error opening Upcoming Sessions: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadApprovedUpcomingSessions() {

        sessionsRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            sessionList.clear();
                            for (DataSnapshot child : snapshot.getChildren()) {

                                Session s = child.getValue(Session.class);
                                if (s == null) continue;


                                if (s.sessionId == null || s.sessionId.isEmpty()) {
                                    s.sessionId = child.getKey();
                                }


                                if ("approved".equalsIgnoreCase(s.status)) {
                                    sessionList.add(s);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(sessionList.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing sessions", e);
                            Toast.makeText(UpcomingSessionsActivity.this,
                                    "Error processing sessions: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }


                });
    }
}
