package com.example.otams;

public class Student {
    public String uid;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String program;
    public Object createdAt;
    public String status;

    public Student() {}

    public Student(String uid, String firstName, String lastName, String email,
                   String phone, String program, Object createdAt, String status) {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.program = program;
        this.createdAt = createdAt;
        this.status = status;
    }
}

