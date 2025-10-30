package com.example.otams;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;

import java.util.List;



public class AdminInboxActivity extends AppCompatActivity {

    private static final String TAG = "AdminInbox";
    private RecyclerView rvRequests;
    private Button btnViewRejected;

    private DatabaseReference requestsRef;
    private final List<RequestWithPath> requestList = new ArrayList<>();
    private RequestsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_inbox);

        rvRequests = findViewById(R.id.rvRequests);
        btnViewRejected = findViewById(R.id.btnViewRejected);

        rvRequests.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RequestsAdapter(requestList);
        rvRequests.setAdapter(adapter);

        requestsRef = FirebaseDatabase.getInstance().getReference("registrationRequests");


        loadPendingRequests();

        btnViewRejected.setOnClickListener(v -> {
            startActivity(new Intent(AdminInboxActivity.this, RejectedRequestsActivity.class));
        });
    }

    private void loadPendingRequests() {
        requestList.clear();
        // read students and tutors nodes with status pending
        requestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot roleNode : snapshot.getChildren()) {
                    String role = roleNode.getKey();
                    if (role == null) continue;
                    for (DataSnapshot child : roleNode.getChildren()) {
                        RegistrationRequest r = child.getValue(RegistrationRequest.class);
                        if (r == null) continue;
                        if ("pending".equalsIgnoreCase(r.status)) {
                            requestList.add(new RequestWithPath(role, child.getKey(), r));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminInboxActivity.this, "Failed to load requests: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    static class RequestWithPath {
        String role;
        String uid;
        RegistrationRequest request;
        RequestWithPath(String role, String uid, RegistrationRequest request) {
            this.role = role;
            this.uid = uid;
            this.request = request;
        }
    }



    private class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.VH> {
        private final List<RequestWithPath> items;

        RequestsAdapter(List<RequestWithPath> items) { this.items = items; }


        @Override
        public VH onCreateViewHolder( ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder( VH holder, int position) {
            RequestWithPath rp = items.get(position);
            RegistrationRequest r = rp.request;
            holder.tvNameRole.setText(String.format("%s — %s", r.getDisplayName(), rp.role));
            holder.tvEmail.setText(r.email == null ? "" : r.email);

            String extra = "";
            if ("students".equalsIgnoreCase(rp.role) && r.program != null) extra = "Program: " + r.program;
            if ("tutors".equalsIgnoreCase(rp.role)) {
                if (r.degree != null) extra += "Degree: " + r.degree;
                if (r.courses != null && !r.courses.isEmpty()) extra += (extra.isEmpty() ? "" : " | ") + "Courses: " + r.courses;
            }
            holder.tvExtra.setText(extra);

            holder.btnApprove.setOnClickListener(v -> approveRequest(rp));
            holder.btnReject.setOnClickListener(v -> rejectRequest(rp));
        }

        @Override
        public int getItemCount() { return items.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView tvNameRole, tvEmail, tvExtra;
            Button btnApprove, btnReject;
            VH( View itemView) {
                super(itemView);
                tvNameRole = itemView.findViewById(R.id.tvNameRole);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvExtra = itemView.findViewById(R.id.tvExtra);
               btnReject = itemView.findViewById(R.id.btnReject);
               btnApprove = itemView.findViewById(R.id.btnApprove);
            }
        }
    }


    private void approveRequest(RequestWithPath rp) {
        String path = rp.role + "/" + rp.uid;
        DatabaseReference rref = requestsRef.child(rp.role).child(rp.uid);


        rref.runTransaction(new com.google.firebase.database.Transaction.Handler() {

            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(com.google.firebase.database.MutableData currentData) {
                Object statusObj = currentData.child("status").getValue();
                String curStatus = statusObj == null ? null : statusObj.toString();
                if ("approved".equalsIgnoreCase(curStatus)) {

                    return com.google.firebase.database.Transaction.abort();
                }

                currentData.child("status").setValue("approved");
                currentData.child("approvedAt").setValue(ServerValue.TIMESTAMP);
                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override
            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed) {
                    Toast.makeText(AdminInboxActivity.this, "Request approved", Toast.LENGTH_SHORT).show();
                    loadPendingRequests();
                } else {
                    if (error != null) {
                        Toast.makeText(AdminInboxActivity.this, "Approve failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "approve error", error.toException());
                    } else {
                        Toast.makeText(AdminInboxActivity.this, "Already approved or cannot approve.", Toast.LENGTH_SHORT).show();
                        loadPendingRequests();
                    }
                }
            }
        });
    }


    private void rejectRequest(RequestWithPath rp) {
        DatabaseReference rref = requestsRef.child(rp.role).child(rp.uid);
        rref.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(@NonNull com.google.firebase.database.MutableData currentData) {
                Object statusObj = currentData.child("status").getValue();
                String curStatus = statusObj == null ? null : statusObj.toString();
                if ("approved".equalsIgnoreCase(curStatus)) {
                    // cannot reject an approved request
                    return com.google.firebase.database.Transaction.abort();
                }
                currentData.child("status").setValue("rejected");
                currentData.child("rejectedAt").setValue(ServerValue.TIMESTAMP);
                currentData.child("rejectionReason").setValue("Rejected by admin");

                return com.google.firebase.database.Transaction.success(currentData);
            }

            @Override

            public void onComplete(DatabaseError error, boolean committed, DataSnapshot snapshot) {
                if (committed) {
                    Toast.makeText(AdminInboxActivity.this, "Request rejected", Toast.LENGTH_SHORT).show();
                }
                loadPendingRequests();
            }

        });
    }

}

