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

import java.util.List;

public class SessionsAdapter extends RecyclerView.Adapter<SessionsAdapter.ViewHolder> {

    private List<Session> sessions;
    private Context context;


    public SessionsAdapter(Context context, List<Session> sessions) {
        this.context = context;
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Session session = sessions.get(position);

        holder.tvCourse.setText("Course: " + (session.course != null ? session.course : "N/A"));
        holder.tvDateTime.setText((session.date != null ? session.date : "")
                + " " + (session.startTime != null ? session.startTime : "")
                + " - " + (session.endTime != null ? session.endTime : ""));
        holder.tvStatus.setText(session.status != null ? session.status : "N/A");

        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("sessions").child(session.sessionId != null ? session.sessionId : "");


        if (holder.btnApprove != null) holder.btnApprove.setVisibility(View.GONE);
        if (holder.btnReject != null) holder.btnReject.setVisibility(View.GONE);
        if (holder.btnCancel != null) holder.btnCancel.setVisibility(View.GONE);

        if ("pending".equalsIgnoreCase(session.status)) {
            if (holder.btnApprove != null) holder.btnApprove.setVisibility(View.VISIBLE);
            if (holder.btnReject != null) holder.btnReject.setVisibility(View.VISIBLE);
        } else if ("approved".equalsIgnoreCase(session.status)) {
            if (holder.btnCancel != null) holder.btnCancel.setVisibility(View.VISIBLE);
        }

        if (holder.btnApprove != null) {
            holder.btnApprove.setOnClickListener(v -> {
                if (session.sessionId == null || session.sessionId.isEmpty()) {
                    Toast.makeText(context, "Invalid session id", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessionRef.child("status").setValue("approved")
                        .addOnSuccessListener(a -> Toast.makeText(context, "Session approved", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        if (holder.btnReject != null) {
            holder.btnReject.setOnClickListener(v -> {
                if (session.sessionId == null || session.sessionId.isEmpty()) {
                    Toast.makeText(context, "Invalid session id", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessionRef.child("status").setValue("rejected")
                        .addOnSuccessListener(a -> Toast.makeText(context, "Session rejected", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }

        if (holder.btnCancel != null) {
            holder.btnCancel.setOnClickListener(v -> {
                if (session.sessionId == null || session.sessionId.isEmpty()) {
                    Toast.makeText(context, "Invalid session id", Toast.LENGTH_SHORT).show();
                    return;
                }
                sessionRef.child("status").setValue("cancelled")
                        .addOnSuccessListener(a -> Toast.makeText(context, "Session cancelled", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });
        }
    }

    @Override
    public int getItemCount() {
        return sessions == null ? 0 : sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourse, tvDateTime, tvStatus;
        Button btnApprove, btnReject, btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }
}
