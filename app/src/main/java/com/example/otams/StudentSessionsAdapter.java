package com.example.otams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class StudentSessionsAdapter extends RecyclerView.Adapter<StudentSessionsAdapter.ViewHolder> {

    private Context context;
    private List<Session> sessions;

    public StudentSessionsAdapter(Context context, List<Session> sessions) {
        this.context = context;
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_student_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);
        holder.tvCourse.setText(session.course);

        holder.tvDateTime.setText(session.date + "  " + session.startTime + " - " + session.endTime);
        holder.tvStatus.setText("Status: " + session.status);

        // Button visible for pending + approved
        if (session.status.equalsIgnoreCase("pending") ||
                session.status.equalsIgnoreCase("approved")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }



        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("sessions").child(session.sessionId);

        holder.btnCancel.setOnClickListener(v -> {

            // If pending → cancel immediately
            if (session.status.equalsIgnoreCase("pending")) {
                cancelSession(session);
                return;
            }

            // If approved → check 24 hour rule
            if (session.status.equalsIgnoreCase("approved")) {

                try {
                    String dateTimeString = session.date + " " + session.startTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    Date sessionDate = sdf.parse(dateTimeString);

                    long sessionTime = sessionDate.getTime();
                    long now = System.currentTimeMillis();

                    long hoursDiff = (sessionTime - now) / (1000 * 60 * 60);

                    if (hoursDiff < 24) {
                        Toast.makeText(context,
                                "Cannot cancel within 24 hours.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Approved & more than 24 hours → allowed
                    cancelSession(session);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
    private void cancelSession(Session session) {
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("sessions").child(session.sessionId);

        sessionRef.child("status").setValue("cancelled")
                .addOnSuccessListener(a ->
                        Toast.makeText(context, "Session cancelled", Toast.LENGTH_SHORT).show()
                );
    }


    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourse, tvDateTime, tvStatus;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
