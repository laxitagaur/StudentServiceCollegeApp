package com.example.demo;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Keeps the student list and saves it in a simple text file.
public class StudentService {
    private final List<Student> students = new ArrayList<>();
    private final Path dataFile = Path.of("data", "students.tsv");

    public StudentService() throws IOException {
        loadStudents();
    }

    public synchronized List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public synchronized Student addStudent(String requestedUid, String name, String section) throws IOException {
        String uid = requestedUid.trim();
        if (uid.isEmpty()) {
            uid = createUid();
        } else if (containsUid(uid)) {
            throw new IllegalArgumentException("That UID is already in use");
        }
        Student student = new Student(uid, name, section);
        students.add(student);
        saveStudents();
        return student;
    }

    private String createUid() {
        String uid;
        do {
            uid = "STU-" + UUID.randomUUID().toString().toUpperCase();
        } while (containsUid(uid));
        return uid;
    }

    private boolean containsUid(String uid) {
        for (Student student : students) {
            if (student.getUid().equalsIgnoreCase(uid)) {
                return true;
            }
        }
        return false;
    }

    public synchronized Student updateStudent(String currentUid, String requestedUid, String name, String section) throws IOException {
        String trimmedCurrentUid = currentUid == null ? "" : currentUid.trim();
        String trimmedRequestedUid = requestedUid == null ? "" : requestedUid.trim();
        String trimmedName = name == null ? "" : name.trim();
        String trimmedSection = section == null ? "" : section.trim();

        if (trimmedCurrentUid.isEmpty()) {
            throw new IllegalArgumentException("Student UID is required");
        }
        if (trimmedName.isEmpty() || trimmedSection.isEmpty()) {
            throw new IllegalArgumentException("Name and section are required");
        }

        Student existing = null;
        int index = -1;
        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            if (student.getUid().equalsIgnoreCase(trimmedCurrentUid)) {
                existing = student;
                index = i;
                break;
            }
        }

        if (existing == null) {
            throw new IllegalArgumentException("Student not found");
        }

        String nextUid = trimmedRequestedUid.isEmpty() ? trimmedCurrentUid : trimmedRequestedUid;
        if (!nextUid.equalsIgnoreCase(trimmedCurrentUid) && containsUid(nextUid)) {
            throw new IllegalArgumentException("That UID is already in use");
        }

        students.set(index, new Student(nextUid, trimmedName, trimmedSection));
        saveStudents();
        return students.get(index);
    }

    public synchronized boolean removeStudent(String uid) throws IOException {
        boolean removed = false;
        for (int index = 0; index < students.size(); index++) {
            if (students.get(index).getUid().equals(uid)) {
                students.remove(index);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveStudents();
        }
        return removed;
    }

    // Load saved rows when the program starts. Create starter students on first run.
    private void loadStudents() throws IOException {
        Files.createDirectories(dataFile.getParent());
        if (Files.exists(dataFile)) {
            for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\t", -1);
                if (parts.length == 3) {
                    students.add(new Student(decode(parts[0]), decode(parts[1]), decode(parts[2])));
                }
            }
        } else {
            students.add(new Student("STU-1001", "Arjun Mehta", "A"));
            students.add(new Student("STU-1002", "Meera Shah", "B"));
            students.add(new Student("STU-1003", "Rahul Verma", "A"));
            students.add(new Student("STU-1004", "Sneha Iyer", "C"));
            saveStudents();
        }
    }

    // URL encoding keeps tabs and special characters safe in the text file.
    private void saveStudents() throws IOException {
        List<String> rows = new ArrayList<>();
        for (Student student : students) {
            rows.add(encode(student.getUid()) + "\t" + encode(student.getName()) + "\t" + encode(student.getSection()));
        }
        Files.write(dataFile, rows, StandardCharsets.UTF_8);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
