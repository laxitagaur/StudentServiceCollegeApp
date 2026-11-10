package com.example.demo;

// One Student object holds one student's ID, name, and section.
public class Student {
    private final String uid;
    private final String name;
    private final String section;

    public Student(String uid, String name, String section) {
        this.uid = uid;
        this.name = name;
        this.section = section;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getSection() {
        return section;
    }
}
