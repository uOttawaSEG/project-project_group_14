package com.example.otams;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class RejectedRequestsActivity extends AppCompatActivity {

    private DatabaseReference requestsRef;
    private ArrayList<RequestItem> rejectedList;
    private RejectedAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_inbox);

        RecyclerView rvRejected = findViewById(R.id.rvRejected);
        rvRejected.setLayoutManager(new LinearLayoutManager(this));

        requestsRef = FirebaseDatabase.getInstance().getReference("registrationRequests");
        rejectedList = new ArrayList<>();
        adapter = new RejectedAdapter(rejectedList);
        rvRejected.setAdapter(adapter);

        loadRejected();
    }

    private void loadRejected() {
        rejectedList.clear();

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                rejectedList.clear();
                for (DataSnapshot roleNode : snapshot.getChildren()) {
                    String role = roleNode.getKey();
                    if (role == null) continue;

                    for (DataSnapshot userNode : roleNode.getChildren()) {
                        RegistrationRequest req = userNode.getValue(RegistrationRequest.class);
                        if (req != null && "rejected".equals(req.status)) {
                            rejectedList.add(new RequestItem(userNode.getKey(), role, req));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) { }
        });
    }

    static class RequestItem {
        String id;
        String role;
        RegistrationRequest req;

        RequestItem(String id, String role, RegistrationRequest req) {
            this.id = id;
            this.role = role;
            this.req = req;
        }
    }

    private class RejectedAdapter extends RecyclerView.Adapter<RejectedAdapter.ViewHolder> {

        ArrayList<RequestItem> items;

        RejectedAdapter(ArrayList<RequestItem> items) { this.items = items; }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_request, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            RequestItem r = items.get(position);

            holder.tvNameRole.setText(r.req.firstName + " " + r.req.lastName + " (" + r.role + ")");
            holder.tvEmail.setText(r.req.email);

            String info = "";
            if (r.role.equals("students")) {
                info = "Program: " + r.req.program;
            } else if (r.role.equals("tutors")) {
                info = "Degree: " + r.req.degree + "\nCourses: " + r.req.courses;
            }
            holder.tvExtra.setText(info);

            holder.btnReject.setVisibility(View.GONE); // hides reject in rejected list
            holder.btnApprove.setText("Re-Approve");
            holder.btnApprove.setOnClickListener(v -> reapprove(r));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNameRole, tvEmail, tvExtra;
            Button btnApprove, btnReject;

            ViewHolder(View itemView) {
                super(itemView);
                tvNameRole = itemView.findViewById(R.id.tvNameRole);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvExtra = itemView.findViewById(R.id.tvExtra);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }

    private void reapprove(RequestItem item) {
        requestsRef.child(item.role).child(item.id).child("status").setValue("approved");
        loadRejected();
    }
}

