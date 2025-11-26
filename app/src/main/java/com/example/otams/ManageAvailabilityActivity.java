package com.example.otams;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ManageAvailabilityActivity extends AppCompatActivity {

    private EditText etDate, etStartTime, etEndTime;
    private CheckBox cbAutoApprove;
    private Button btnAddSlot;
    private RecyclerView rvSlots;

    private DatabaseReference slotsRef;
    private String tutorId;
    private List<AvailabilitySlot> slotList;
    private AvailabilityAdapter adapter;

    private Calendar calendar;
    private SimpleDateFormat dateFormat, timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_availability);

        initViews();
        setupFirebase();
        setupRecyclerView();
        loadSlots();
    }

    private void initViews() {
        etDate = findViewById(R.id.etDate);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        cbAutoApprove = findViewById(R.id.cbAutoApprove);
        btnAddSlot = findViewById(R.id.btnAddSlot);
        rvSlots = findViewById(R.id.rvSlots);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        etDate.setOnClickListener(v -> showDatePicker());
        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        btnAddSlot.setOnClickListener(v -> addAvailabilitySlot());
    }

    private void setupFirebase() {
        tutorId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        slotsRef = FirebaseDatabase.getInstance().getReference("availabilitySlots");
    }

    private void setupRecyclerView() {
        slotList = new ArrayList<>();


        adapter = new AvailabilityAdapter(slotList, this::deleteSlot, this);

        rvSlots.setLayoutManager(new LinearLayoutManager(this));
        rvSlots.setAdapter(adapter);
    }

    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etDate.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void showTimePicker(EditText editText) {
        TimePickerDialog timePicker = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {

                    int roundedMinute = (minute + 15) / 30 * 30;
                    if (roundedMinute == 60) {
                        roundedMinute = 0;
                        hourOfDay += 1;
                    }
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, roundedMinute);
                    editText.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );
        timePicker.show();
    }

    private void addAvailabilitySlot() {
        String date = etDate.getText().toString().trim();
        String startTime = etStartTime.getText().toString().trim();
        String endTime = etEndTime.getText().toString().trim();
        boolean autoApprove = cbAutoApprove.isChecked();

        if (date.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidTimeSlot(startTime) || !isValidTimeSlot(endTime)) {
            Toast.makeText(this, "Times must be in 30-minute increments (e.g., 8:00, 8:30)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPastDate(date)) {
            Toast.makeText(this, "Cannot select past dates", Toast.LENGTH_SHORT).show();
            return;
        }

        checkForOverlaps(date, startTime, endTime, () -> {
            AvailabilitySlot slot = new AvailabilitySlot(tutorId, date, startTime, endTime, autoApprove);
            String slotId = slotsRef.push().getKey();
            slot.slotId = slotId;

            slotsRef.child(slotId).setValue(slot.toMap())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Availability slot added", Toast.LENGTH_SHORT).show();
                        clearForm();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to add slot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private boolean isValidTimeSlot(String time) {
        String[] parts = time.split(":");
        if (parts.length != 2) return false;

        int minutes = Integer.parseInt(parts[1]);
        return minutes == 0 || minutes == 30;
    }

    private boolean isPastDate(String date) {
        try {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTime(dateFormat.parse(date));
            return selectedDate.before(Calendar.getInstance());
        } catch (Exception e) {
            return true;
        }
    }

    private void checkForOverlaps(String date, String startTime, String endTime, Runnable onSuccess) {
        slotsRef.orderByChild("tutorId").equalTo(tutorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                            AvailabilitySlot existingSlot = slotSnapshot.getValue(AvailabilitySlot.class);
                            if (existingSlot != null && existingSlot.date.equals(date)) {

                                if (isOverlapping(startTime, endTime, existingSlot.startTime, existingSlot.endTime)) {
                                    Toast.makeText(ManageAvailabilityActivity.this,
                                            "Overlaps with existing slot", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        onSuccess.run();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ManageAvailabilityActivity.this,
                                "Error checking overlaps: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isOverlapping(String start1, String end1, String start2, String end2) {
        return timeToMinutes(start1) < timeToMinutes(end2) && timeToMinutes(end1) > timeToMinutes(start2);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private void clearForm() {
        etDate.setText("");
        etStartTime.setText("");
        etEndTime.setText("");
        cbAutoApprove.setChecked(false);
    }

    private void loadSlots() {
        slotsRef.orderByChild("tutorId").equalTo(tutorId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        slotList.clear();
                        for (DataSnapshot slotSnapshot : snapshot.getChildren()) {
                            AvailabilitySlot slot = slotSnapshot.getValue(AvailabilitySlot.class);
                            if (slot != null) {
                                slot.slotId = slotSnapshot.getKey();


                                DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
                                sessionsRef.orderByChild("slotId").equalTo(slot.slotId)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot sessionSnap) {
                                                for (DataSnapshot s : sessionSnap.getChildren()) {
                                                    String status = s.child("status").getValue(String.class);
                                                    if ("pending".equals(status) || "approved".equals(status)) {
                                                        slot.isBooked = true;
                                                        break;
                                                    }
                                                }
                                                adapter.notifyDataSetChanged();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError error) { }
                                        });

                                slotList.add(slot);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ManageAvailabilityActivity.this,
                                "Failed to load slots: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteSlot(AvailabilitySlot slot) {
        Log.d("DeleteSlot", "Attempting to delete slot: " + slot.slotId);

        DatabaseReference sessionsRef = FirebaseDatabase.getInstance().getReference("sessions");
        sessionsRef.orderByChild("slotId").equalTo(slot.slotId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean hasBookedSessions = false;

                        for (DataSnapshot sessionSnapshot : snapshot.getChildren()) {
                            String status = sessionSnapshot.child("status").getValue(String.class);
                            if ("pending".equals(status) || "approved".equals(status)) {
                                hasBookedSessions = true;
                                break;
                            }
                        }

                        if (hasBookedSessions) {
                            Toast.makeText(ManageAvailabilityActivity.this,
                                    "Cannot delete slot with booked sessions", Toast.LENGTH_SHORT).show();
                        } else {
                            if (slot.slotId != null) {
                                slotsRef.child(slot.slotId).removeValue()
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(ManageAvailabilityActivity.this,
                                                        "Slot deleted successfully", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(ManageAvailabilityActivity.this,
                                                        "Failed to delete slot: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                Toast.makeText(ManageAvailabilityActivity.this,
                                        "Error: Slot ID is null", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(ManageAvailabilityActivity.this,
                                "Error checking sessions: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
