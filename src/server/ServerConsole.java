package server;

import java.io.*;
import java.net.*;

public class ServerConsole {
    public static void main(String[] args) {
        final int PORT = 5000;
        System.out.println("Server starting on port " + PORT + " ...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Waiting for client...");
            try (Socket socket = serverSocket.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                System.out.println("Client connected: " + socket.getInetAddress());

                // DB file path relative to project root
                DBHandler db = new DBHandler("database/students.db");

                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("Request: " + request);
                    String[] parts = request.split("\\|", -1); // include empty parts
                    String cmd = parts[0].toUpperCase();

                    String response;
                    switch (cmd) {
                        case "ADD":
                            // expects ADD|roll|name|degree|semester
                            response = db.addStudent(parts[1], parts[2], parts[3], parts[4]);
                            break;
                        case "UPDATE":
                            response = db.updateStudent(parts[1], parts[2], parts[3], parts[4]);
                            break;
                        case "DELETE":
                            response = db.deleteStudent(parts[1]);
                            break;
                        case "VIEW":
                            response = db.getAllStudents();
                            break;
                        case "EXIT":
                            response = "GOODBYE";
                            out.println(response);
                            db.close();
                            socket.close();
                            System.out.println("Client asked to exit.");
                            return;
                        default:
                            response = "ERROR: Unknown command";
                    }

                    out.println(response);
                }
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped.");
    }
}
