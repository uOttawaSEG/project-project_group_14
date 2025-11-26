package com.example.otams;

import android.content.Context;
import android.widget.Toast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AvailabilityAdapter extends RecyclerView.Adapter<AvailabilityAdapter.ViewHolder> {

    private final List<AvailabilitySlot> slotList;
    private final OnDeleteClickListener onDeleteClickListener;
    private final Context context;

    public interface OnDeleteClickListener {
        void onDeleteClick(AvailabilitySlot slot);
    }


    public AvailabilityAdapter(List<AvailabilitySlot> slotList, OnDeleteClickListener listener, Context ctx) {
        this.slotList = slotList;
        this.onDeleteClickListener = listener;
        this.context = ctx;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_availability_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AvailabilitySlot slot = slotList.get(position);

        holder.tvDate.setText(slot.date);
        holder.tvTime.setText(String.format("%s - %s", slot.startTime, slot.endTime));
        holder.tvAutoApprove.setText(slot.autoApprove ? "Auto-approve: ON" : "Auto-approve: OFF");


        holder.btnDelete.setOnClickListener(v -> {
            if (slot.isBooked) {

                Toast.makeText(context, "Cannot delete — this slot has a booked session.", Toast.LENGTH_LONG).show();
            } else {
                onDeleteClickListener.onDeleteClick(slot);
            }
        });

    }

    @Override
    public int getItemCount() {
        return slotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvAutoApprove;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvAutoApprove = itemView.findViewById(R.id.tvAutoApprove);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

