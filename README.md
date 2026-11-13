# Orbit Student Directory

A small student directory made with basic Java and a browser page. It uses the JDK's built-in web server, so it needs no Spring Boot or Maven.

## Run it

1. Install a JDK (Java 17 or newer).
2. Open a terminal in this project folder.
3. Compile the Java files:

   ```bash
   javac --add-modules jdk.httpserver -d out src/main/java/com/example/demo/*.java
   ```

4. Start the app:

   ```bash
   java --add-modules jdk.httpserver -cp out com.example.demo.StudentWebServer
   ```

5. Open <http://localhost:8080> in a browser. Keep the terminal open while you use the page.

The first run creates `data/students.tsv` with four sample students. New students, edits, and removals are saved in that file, so they are still there after restarting the app.

## What to explain

- `Student.java` is a simple model: one object holds a student's UID, name, and section.
- `StudentService.java` keeps the list, accepts a custom UID or makes one, updates saved student details, and loads/saves student rows in a text file. It prevents duplicate UIDs.
- `StudentWebServer.java` starts Java's small built-in web server. It sends the website to the browser and handles requests to list, add, edit, and remove students.
- `index.html` is the interactive page. JavaScript asks the Java server for students, then draws the directory, search, section filters, and add/edit/remove actions.
- A UID is a unique label assigned by Java when a student is added. The same UID is saved with the student's name and section, and it can be updated during an edit.

## Simple presentation explanation

“The website sends requests to a Java program. Java stores the student list in a text file. Every student has a UID, name, and section. I can enter a UID or let Java create one. I can add, search, filter, edit, and remove students on the website, and the saved list stays after I close and restart the app.”
