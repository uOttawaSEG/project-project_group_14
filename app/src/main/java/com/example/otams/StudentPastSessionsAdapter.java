package com.example.otams;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class StudentPastSessionsAdapter extends RecyclerView.Adapter<StudentPastSessionsAdapter.ViewHolder> {

    private Context context;
    private List<Session> sessions;

    public StudentPastSessionsAdapter(Context context, List<Session> sessions) {
        this.context = context;
        this.sessions = sessions;
    }

    @NonNull
    @Override
    public StudentPastSessionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_past_session, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentPastSessionsAdapter.ViewHolder holder, int position) {
        Session session = sessions.get(position);



        session.sessionId = session.sessionId != null ? session.sessionId : sessions.get(position).sessionId;

        holder.tvCourse.setText(session.course != null ? session.course : "Course: N/A");
        holder.tvDateTime.setText((session.date != null ? session.date : "") + " " +
                (session.startTime != null ? session.startTime : "") + " - " +
                (session.endTime != null ? session.endTime : ""));


        holder.ratingBarDisplay.setRating(session.rating > 0 ? session.rating : 0f);


        holder.btnRate.setOnClickListener(v -> openRatingDialog(session));
    }

    private void openRatingDialog(Session session) {
        RatingBar ratingBar = new RatingBar(context);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        ratingBar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        new AlertDialog.Builder(context)
                .setTitle("Rate Tutor")
                .setView(ratingBar)
                .setPositiveButton("Submit", (dialog, which) -> {
                    int ratingValue = (int) ratingBar.getRating();

                    // Save rating into sessions
                    FirebaseDatabase.getInstance()
                            .getReference("sessions")
                            .child(session.sessionId)
                            .child("rating")
                            .setValue(ratingValue)
                            .addOnSuccessListener(a -> {

                                session.rating = ratingValue;
                                int idx = sessions.indexOf(session);
                                if (idx >= 0) notifyItemChanged(idx);
                                Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to submit rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return sessions == null ? 0 : sessions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourse, tvDateTime;
        Button btnRate;
        RatingBar ratingBarDisplay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCoursePast);
            tvDateTime = itemView.findViewById(R.id.tvDateTimePast);
            btnRate = itemView.findViewById(R.id.btnRateTutor);
            ratingBarDisplay = itemView.findViewById(R.id.ratingBarDisplay);
        }
    }
}

