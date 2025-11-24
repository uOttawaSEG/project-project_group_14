package com.example.otams;

import java.util.HashMap;
import java.util.Map;

public class Session {
    public String sessionId;
    public String studentId;
    public String tutorId;
    public String studentName;
    public String slotId;
    public String course;
    public String date;
    public String startTime;
    public String endTime;
    public String status;
    public int rating;
    public String review;
    public long createdAt;
    public long updatedAt;

    public Session() {}

    public Session(String studentId, String tutorId, String slotId, String course, String date, String startTime, String endTime) {
        this.studentId = studentId;
        this.tutorId = tutorId;
        this.slotId = slotId;
        this.course = course;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = "pending";
        this.rating = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("studentId", studentId);
        map.put("tutorId", tutorId);
        map.put("slotId", slotId);
        map.put("course", course);
        map.put("date", date);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("status", status);
        map.put("rating", rating);
        map.put("review", review);
        map.put("createdAt", createdAt);
        map.put("updatedAt", updatedAt);
        return map;
    }
}
