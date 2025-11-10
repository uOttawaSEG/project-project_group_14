package com.example.otams;

public class RegistrationRequest {

    public String firstName;
    public String lastName;
    public String email;

    public String phone;

    public String program;
    public String degree;
    public String courses;
    public String status;
    public String role;

    public RegistrationRequest() { }


    public RegistrationRequest(String firstName, String lastName, String email,
                               String phone, String degree, String courses,
                               String status, String role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.degree = degree;
        this.courses = courses;
        this.status = status;
        this.role = role;
    }
    public String getDisplayName() {
        String s = "";
        if (firstName != null) s += firstName;
        if (lastName != null) s += (s.isEmpty() ? "" : " ") + lastName;
        return s.isEmpty() ? email : s;
    }
}
