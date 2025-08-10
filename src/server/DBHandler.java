package server;

import java.sql.*;
import java.util.StringJoiner;

public class DBHandler {
    private Connection conn;

    // pass DB path relative to project root, e.g. "database/students.db"
    public DBHandler(String dbPath) throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        conn = DriverManager.getConnection(url);
    }

    public String addStudent(String roll, String name, String degree, String semester) {
        String sql = "INSERT INTO Students(roll_no, name, degree, semester) VALUES(?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.setString(3, degree);
            ps.setString(4, semester);
            ps.executeUpdate();
            return "SUCCESS: Student added.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String updateStudent(String roll, String name, String degree, String semester) {
        String sql = "UPDATE Students SET name = ?, degree = ?, semester = ? WHERE roll_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, degree);
            ps.setString(3, semester);
            ps.setString(4, roll);
            int rows = ps.executeUpdate();
            return rows > 0 ? "SUCCESS: Student updated." : "ERROR: Roll number not found.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String deleteStudent(String roll) {
        String sql = "DELETE FROM Students WHERE roll_no = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roll);
            int rows = ps.executeUpdate();
            return rows > 0 ? "SUCCESS: Student deleted." : "ERROR: Roll number not found.";
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // returns rows in a semicolon-separated format:
    // roll1,name1,degree1,semester1;roll2,name2,degree2,semester2
    public String getAllStudents() {
        String sql = "SELECT roll_no, name, degree, semester FROM Students";
        StringJoiner sj = new StringJoiner(";");
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String row = String.join(",",
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("degree") == null ? "" : rs.getString("degree"),
                    rs.getString("semester") == null ? "" : rs.getString("semester")
                );
                sj.add(row);
            }
            return "SUCCESS: " + sj.toString();
        } catch (SQLException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public void close() {
        try { if (conn != null) conn.close(); } catch (SQLException ignored) {}
    }
}
