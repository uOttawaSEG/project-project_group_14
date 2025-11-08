package com.example.otams;

import java.util.HashMap;
import java.util.Map;

public class AvailabilitySlot {
    public String slotId;
    public String tutorId;
    public String date;
    public String startTime;
    public String endTime;
    public boolean autoApprove;
    public long createdAt;

    public AvailabilitySlot() {}

    public AvailabilitySlot(String tutorId, String date, String startTime, String endTime, boolean autoApprove) {
        this.tutorId = tutorId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.autoApprove = autoApprove;
        this.createdAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("tutorId", tutorId);
        map.put("date", date);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("autoApprove", autoApprove);
        map.put("createdAt", createdAt);
        return map;
    }
}

