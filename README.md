# Student Management System (Java GUI, TCP, JDBC, SQLite)

## 📌 Overview
This is a **GUI-based Student Management System** built in Java that demonstrates:
- **TCP client-server communication**
- **SQLite database integration** via JDBC
- **CRUD operations** (Create, Read, Update, Delete)
- **Data validation & error handling**

Originally developed as **Assignment 4** for the University of the Punjab, this project extends **Assignment 3** by adding a graphical interface and real-time server communication.

---

## 🎯 Features
### Client (GUI Application)
- Add a new student record
- Update an existing student record
- Delete a student record
- View all student records in a table
- Input validation:
  - No empty fields
  - Roll number must be numeric & unique
- Sends all operations to the server via TCP sockets
- Displays server responses (success/failure messages)

### Server (Database Handler)
- Listens for client requests (single client, no threading)
- Parses incoming commands
- Performs CRUD operations on the **SQLite database**
- Sends confirmation or updated data back to the client
- Handles invalid operations gracefully

---

## 🗄 Database Schema
```sql
CREATE TABLE Students (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    roll_no TEXT NOT NULL UNIQUE,
    name TEXT NOT NULL,
    degree TEXT,
    semester TEXT
);
