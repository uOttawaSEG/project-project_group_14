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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class PastSessionsActivity extends AppCompatActivity {
    private static final String TAG = "PastSessionsActivity";

    private RecyclerView rvSessions;
    private TextView tvEmpty;
    private SessionsAdapter adapter;
    private final List<Session> sessionList = new ArrayList<>();

    private DatabaseReference sessionsRef;
    private String tutorId;


    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_past_sessions);

            rvSessions = findViewById(R.id.rvSessionsPast);
            tvEmpty = findViewById(R.id.tvEmptyPast);

            rvSessions.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SessionsAdapter(this, sessionList);
            rvSessions.setAdapter(adapter);

            sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");

            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Toast.makeText(this, "No signed-in user found. Please sign-in first.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            loadPastSessions();
        } catch (Exception e) {
            Log.e(TAG, "Exception in onCreate", e);
            Toast.makeText(this, "Error opening Past Sessions: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void loadPastSessions() {
        sessionsRef.orderByChild("tutorId").equalTo(tutorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            sessionList.clear();
                            Date now = new Date();

                            for (DataSnapshot child : snapshot.getChildren()) {
                                Session s = child.getValue(Session.class);
                                if (s == null) continue;


                                if (s.sessionId == null || s.sessionId.isEmpty()) {
                                    s.sessionId = child.getKey();
                                }


                                if (s.status != null) {
                                    String st = s.status.toLowerCase();
                                    if ("cancelled".equals(st) || "rejected".equals(st)) continue;
                                }


                                Date sessionEnd = parseSessionEndDateTime(s);
                                if (sessionEnd == null) {

                                }


                                if (sessionEnd.before(now)) {
                                    sessionList.add(s);
                                }
                            }

                            adapter.notifyDataSetChanged();
                            tvEmpty.setVisibility(sessionList.isEmpty() ? View.VISIBLE : View.GONE);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing sessions", e);
                            Toast.makeText(PastSessionsActivity.this,
                                    "Error processing sessions: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Firebase cancelled: " + error.getMessage(), error.toException());
                        Toast.makeText(PastSessionsActivity.this,
                                "Failed to load sessions: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    private Date parseSessionEndDateTime(Session s) {
        try {
            if (s.date == null || s.endTime == null) return null;
            String combined = s.date.trim() + " " + s.endTime.trim();
            return dateTimeFormat.parse(combined);
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse session date/time: " + e.getMessage(), e);
            return null;
        }
    }
}

