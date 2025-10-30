package com.example.otams;

public class RegistrationRequest {

    public String firstName;
    public String lastName;
    public String email;

    public String program;
    public String degree;
    public String courses;
    public String status;

    // a helper to get display name
    public String getDisplayName() {
        String s = "";
        if (firstName != null) s += firstName;
        if (lastName != null) s += (s.isEmpty() ? "" : " ") + (lastName != null ? lastName : "");
        return s.isEmpty() ? email : s;
    }
}
