package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminInboxActivity extends AppCompatActivity {

    private DatabaseReference requestsRef;
    private final ArrayList<RequestItem> requestList = new ArrayList<>();
    private RequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inbox);

        RecyclerView rvRequests = findViewById(R.id.rvRequests);
        Button btnViewRejected = findViewById(R.id.btnViewRejected);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(requestList);
        rvRequests.setAdapter(adapter);

        requestsRef = FirebaseDatabase.getInstance().getReference("registrationRequests");

        loadPendingRequests();

        btnViewRejected.setOnClickListener(v ->
                startActivity(new Intent(AdminInboxActivity.this, RejectedRequestsActivity.class))
        );
    }

    private void loadPendingRequests() {
        requestList.clear();

        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot roleNode : snapshot.getChildren()) {
                    String role = roleNode.getKey();
                    for (DataSnapshot userNode : roleNode.getChildren()) {
                        RegistrationRequest req = userNode.getValue(RegistrationRequest.class);
                        if (req != null && "pending".equals(req.status)) {
                            requestList.add(new RequestItem(userNode.getKey(), role, req));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


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

    private class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

        ArrayList<RequestItem> items;
        RequestsAdapter(ArrayList<RequestItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_request, parent, false);
            return new ViewHolder(view);
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


            holder.btnApprove.setOnClickListener(v -> approve(r));
            holder.btnReject.setOnClickListener(v -> reject(r));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNameRole, tvEmail;
            Button btnApprove, btnReject;

            ViewHolder(View itemView) {
                super(itemView);
                tvNameRole = itemView.findViewById(R.id.tvNameRole);
                tvEmail = itemView.findViewById(R.id.tvEmail);

                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }

    private void approve(RequestItem item) {
        requestsRef.child(item.role).child(item.id).child("status").setValue("approved");
        loadPendingRequests();
    }

    private void reject(RequestItem item) {
        requestsRef.child(item.role).child(item.id).child("status").setValue("rejected");
        loadPendingRequests();
    }
}

