package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientConsole {
    public static void main(String[] args) {
        final String HOST = "localhost";
        final int PORT = 5000;

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Connected to server " + HOST + ":" + PORT);

            while (true) {
                System.out.println("\n--- MENU ---");
                System.out.println("1. Add New Student");
                System.out.println("2. Update Student");
                System.out.println("3. Delete Student");
                System.out.println("4. View All Students");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                String choice = sc.nextLine().trim();

                if (choice.equals("1")) {
                    System.out.print("Roll No: "); String roll = sc.nextLine();
                    System.out.print("Name: "); String name = sc.nextLine();
                    System.out.print("Degree: "); String degree = sc.nextLine();
                    System.out.print("Semester: "); String sem = sc.nextLine();
                    out.println("ADD|" + roll + "|" + name + "|" + degree + "|" + sem);
                } else if (choice.equals("2")) {
                    System.out.print("Roll No to update: "); String roll = sc.nextLine();
                    System.out.print("Name: "); String name = sc.nextLine();
                    System.out.print("Degree: "); String degree = sc.nextLine();
                    System.out.print("Semester: "); String sem = sc.nextLine();
                    out.println("UPDATE|" + roll + "|" + name + "|" + degree + "|" + sem);
                } else if (choice.equals("3")) {
                    System.out.print("Roll No to delete: "); String roll = sc.nextLine();
                    out.println("DELETE|" + roll);
                } else if (choice.equals("4")) {
                    out.println("VIEW");
                } else if (choice.equals("5")) {
                    out.println("EXIT");
                    System.out.println("Exiting client.");
                    break;
                } else {
                    System.out.println("Invalid choice.");
                    continue;
                }

                String response = in.readLine();
                if (response == null) {
                    System.out.println("No response (server closed connection).");
                    break;
                }
                System.out.println("Server: " + response);

                // If VIEW response: parse and print table-like
                if (response.startsWith("SUCCESS:") && response.length() > 8 && response.contains(",")) {
                    String data = response.substring(8).trim();
                    if (!data.isEmpty()) {
                        System.out.println("\nRoll\tName\tDegree\tSemester");
                        for (String row : data.split(";")) {
                            String[] f = row.split(",", -1);
                            System.out.printf("%s\t%s\t%s\t%s%n", f[0], f[1], f[2], f[3]);
                        }
                    } else {
                        System.out.println("(No rows)");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
