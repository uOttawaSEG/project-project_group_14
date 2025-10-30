package com.example.otams;

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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RejectedRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRejected;
    private DatabaseReference requestsRef;
    private List<AdminInboxActivity.RequestWithPath> rejectedList;
    private RejectedAdapter adapter;
    private static final String TAG = "RejectedRequests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rejected_inbox);

        rvRejected = findViewById(R.id.rvRejected);
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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                rejectedList.clear();
                for (DataSnapshot roleNode : snapshot.getChildren()) {
                    String role = roleNode.getKey();
                    if (role == null) continue;
                    for (DataSnapshot child : roleNode.getChildren()) {
                        RegistrationRequest r = child.getValue(RegistrationRequest.class);
                        if (r == null) continue;
                        if ("rejected".equalsIgnoreCase(r.status)) {
                            rejectedList.add(new AdminInboxActivity.RequestWithPath(role, child.getKey(), r));
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RejectedRequestsActivity.this,
                        "Failed to load rejected: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private class RejectedAdapter extends RecyclerView.Adapter<RejectedAdapter.VH> {
        List<AdminInboxActivity.RequestWithPath> items;

        RejectedAdapter(List<AdminInboxActivity.RequestWithPath> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_request, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            AdminInboxActivity.RequestWithPath rp = items.get(position);
            RegistrationRequest r = rp.request;

            holder.tvNameRole.setText(r.getDisplayName() + " — " + rp.role);
            holder.tvEmail.setText(r.email == null ? "" : r.email);

            String extra = "";
            if ("students".equalsIgnoreCase(rp.role) && r.program != null)
                extra = "Program: " + r.program;
            if ("tutors".equalsIgnoreCase(rp.role)) {
                if (r.degree != null) extra += "Degree: " + r.degree;
                if (r.courses != null && !r.courses.isEmpty())
                    extra += (extra.isEmpty() ? "" : " | ") + "Courses: " + r.courses;
            }
            holder.tvExtra.setText(extra);

            // show Approve button for rejected list
            holder.btnReject.setVisibility(View.GONE);
            holder.btnApprove.setText("Re-Approve");

            holder.btnApprove.setOnClickListener(v -> reapproveRequest(rp));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvNameRole, tvEmail, tvExtra;
            Button btnApprove, btnReject;

            VH(@NonNull View itemView) {
                super(itemView);
                tvNameRole = itemView.findViewById(R.id.tvNameRole);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvExtra = itemView.findViewById(R.id.tvExtra);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }

    // re-approve a rejected request
    public void reapproveRequest(AdminInboxActivity.RequestWithPath rp) {
        DatabaseReference rref = requestsRef.child(rp.role).child(rp.uid);
        rref.runTransaction(new com.google.firebase.database.Transaction.Handler() {
            @NonNull
            @Override
            public com.google.firebase.database.Transaction.Result doTransaction(
                    @NonNull com.google.firebase.database.MutableData currentData) {

                Object statusObj = currentData.child("status").getValue();
                String curStatus = (statusObj == null ? null : statusObj.toString());
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
                    Toast.makeText(RejectedRequestsActivity.this,
                            "Request re-approved", Toast.LENGTH_SHORT).show();
                    loadRejected();
                } else {
                    if (error != null) {
                        Log.e(TAG, "reapprove error", error.toException());
                        Toast.makeText(RejectedRequestsActivity.this,
                                "Re-approve failed: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RejectedRequestsActivity.this,
                                "Already approved.", Toast.LENGTH_SHORT).show();
                        loadRejected();
                    }
                }
            }
        });
    }
}

