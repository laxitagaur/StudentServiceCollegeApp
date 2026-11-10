package com.example.demo;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Starts a small web server using Java's built-in HTTP server.
public class StudentWebServer {
    private static StudentService studentService;

    public static void main(String[] args) throws IOException {
        studentService = new StudentService();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/api/students", StudentWebServer::handleStudents);
        server.createContext("/", StudentWebServer::showWebsite);
        server.start();

        System.out.println("Student website is ready at http://localhost:8080");
        System.out.println("Keep this window open while using the website.");
    }

    // Serve the page when the browser visits the home address.
    private static void showWebsite(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().equals("/")) {
            send(exchange, 404, "Page not found", "text/plain; charset=UTF-8");
            return;
        }
        byte[] page = Files.readAllBytes(Path.of("index.html"));
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, page.length);
        exchange.getResponseBody().write(page);
        exchange.close();
    }

    // Handle browser requests to list, add, or remove students.
    private static void handleStudents(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if (method.equals("GET")) {
            send(exchange, 200, studentsAsJson(studentService.getStudents()), "application/json; charset=UTF-8");
        } else if (method.equals("POST")) {
            Map<String, String> form = readForm(exchange);
            String uid = form.getOrDefault("uid", "").trim();
            String name = form.getOrDefault("name", "").trim();
            String section = form.getOrDefault("section", "").trim();
            if (name.isEmpty() || section.isEmpty()) {
                send(exchange, 400, "Name and section are required", "text/plain; charset=UTF-8");
                return;
            }
            try {
                Student added = studentService.addStudent(uid, name, section);
                send(exchange, 201, studentAsJson(added), "application/json; charset=UTF-8");
            } catch (IllegalArgumentException error) {
                send(exchange, 409, error.getMessage(), "text/plain; charset=UTF-8");
            }
        } else if (method.equals("DELETE")) {
            String uid = queryValue(exchange.getRequestURI().getRawQuery(), "uid");
            boolean removed = studentService.removeStudent(uid);
            send(exchange, removed ? 200 : 404, "{\"removed\":" + removed + "}", "application/json; charset=UTF-8");
        } else {
            exchange.getResponseHeaders().set("Allow", "GET, POST, DELETE");
            send(exchange, 405, "Method not allowed", "text/plain; charset=UTF-8");
        }
    }

    private static Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();
        String[] fields = body.split("&");
        for (String field : fields) {
            String[] pair = field.split("=", 2);
            if (pair.length == 2) {
                form.put(decode(pair[0]), decode(pair[1]));
            }
        }
        return form;
    }

    private static String queryValue(String query, String key) {
        if (query == null) return "";
        for (String part : query.split("&")) {
            String[] pair = part.split("=", 2);
            if (pair.length == 2 && decode(pair[0]).equals(key)) return decode(pair[1]);
        }
        return "";
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String studentsAsJson(List<Student> students) {
        StringBuilder json = new StringBuilder("[");
        for (int index = 0; index < students.size(); index++) {
            if (index > 0) json.append(',');
            json.append(studentAsJson(students.get(index)));
        }
        return json.append(']').toString();
    }

    private static String studentAsJson(Student student) {
        return "{\"uid\":\"" + escapeJson(student.getUid())
                + "\",\"name\":\"" + escapeJson(student.getName())
                + "\",\"section\":\"" + escapeJson(student.getSection()) + "\"}";
    }

    // Escape special characters so names can safely be sent as JSON.
    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private static void send(HttpExchange exchange, int status, String response, String contentType) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
