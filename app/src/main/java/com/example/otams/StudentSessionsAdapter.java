package com.example.otams;

import android.content.Context;
import android.content.Intent;
import android.provider.CalendarContract;
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
import java.util.Calendar;
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


        if (session.status.equalsIgnoreCase("pending") ||
                session.status.equalsIgnoreCase("approved")) {
            holder.btnCancel.setVisibility(View.VISIBLE);
        } else {
            holder.btnCancel.setVisibility(View.GONE);
        }
        if (session.status.equalsIgnoreCase("approved"))
            holder.btnExport.setVisibility(View.VISIBLE);
        else {
            holder.btnExport.setVisibility(View.GONE);
        }
        holder.btnExport.setOnClickListener(v -> exportToCalendar(session));



        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("sessions").child(session.sessionId);

        holder.btnCancel.setOnClickListener(v -> {


            if (session.status.equalsIgnoreCase("pending")) {
                cancelSession(session);
                return;
            }


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
    private void exportToCalendar(Session s) {
        try {
            String[] dateParts = s.date.split("-");
            String[] startParts = s.startTime.split(":");
            String[] endParts = s.endTime.split(":");

            Calendar begin = Calendar.getInstance();
            begin.set(
                    Integer.parseInt(dateParts[0]),
                    Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(startParts[0]),
                    Integer.parseInt(startParts[1])
            );

            Calendar end = Calendar.getInstance();
            end.set(
                    Integer.parseInt(dateParts[0]),
                    Integer.parseInt(dateParts[1]) - 1,
                    Integer.parseInt(dateParts[2]),
                    Integer.parseInt(endParts[0]),
                    Integer.parseInt(endParts[1])
            );

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setData(CalendarContract.Events.CONTENT_URI);

            intent.putExtra(CalendarContract.Events.TITLE,
                    "Tutoring Session - " + s.course);

            intent.putExtra(CalendarContract.Events.DESCRIPTION,
                    "Tutoring session booked through OTAMS.");

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, begin.getTimeInMillis());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTimeInMillis());

            context.startActivity(intent);

        } catch (Exception e) {
            Toast.makeText(context, "Failed to export.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public int getItemCount() {
        return sessions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourse, tvDateTime, tvStatus;
        Button btnCancel;
        Button btnExport;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourse = itemView.findViewById(R.id.tvCourse);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnExport = itemView.findViewById(R.id.btnExport);
        }
    }
}
