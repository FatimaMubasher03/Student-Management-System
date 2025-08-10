package client;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;

public class ClientGUI extends JFrame {
    private JTextField txtRoll, txtName, txtDegree, txtSemester;
    private JButton btnAdd, btnUpdate, btnDelete, btnView, btnConnect, btnDisconnect;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblStatus;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private final String HOST = "localhost";
    private final int PORT = 5000;

    public ClientGUI() {
        super("Student Management - Client (GUI)");
        initUI();
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disconnect();
                dispose();
                System.exit(0);
            }
        });
        setSize(800, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initUI() {
        // Top form
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; form.add(new JLabel("Roll No:"), gc);
        gc.gridx = 1; gc.gridy = 0; txtRoll = new JTextField(10); form.add(txtRoll, gc);

        gc.gridx = 2; gc.gridy = 0; form.add(new JLabel("Name:"), gc);
        gc.gridx = 3; gc.gridy = 0; txtName = new JTextField(15); form.add(txtName, gc);

        gc.gridx = 0; gc.gridy = 1; form.add(new JLabel("Degree:"), gc);
        gc.gridx = 1; gc.gridy = 1; txtDegree = new JTextField(10); form.add(txtDegree, gc);

        gc.gridx = 2; gc.gridy = 1; form.add(new JLabel("Semester:"), gc);
        gc.gridx = 3; gc.gridy = 1; txtSemester = new JTextField(5); form.add(txtSemester, gc);

        // Buttons panel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnView = new JButton("View All");
        btnConnect = new JButton("Connect");
        btnDisconnect = new JButton("Disconnect");
        btnDisconnect.setEnabled(false);

        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnView);
        buttons.add(btnConnect);
        buttons.add(btnDisconnect);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Roll No","Name","Degree","Semester"}, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Status
        lblStatus = new JLabel("Not connected.");
        lblStatus.setBorder(BorderFactory.createEmptyBorder(6,6,6,6));

        // Layout
        Container c = getContentPane();
        c.setLayout(new BorderLayout(8,8));
        c.add(form, BorderLayout.NORTH);
        c.add(buttons, BorderLayout.CENTER);
        c.add(new JScrollPane(table), BorderLayout.SOUTH);
        c.add(lblStatus, BorderLayout.PAGE_END);

        // Event handlers
        btnConnect.addActionListener(e -> connect());
        btnDisconnect.addActionListener(e -> disconnect());

        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnView.addActionListener(e -> viewAll());

        table.getSelectionModel().addListSelectionListener(e -> onTableSelect(e));
    }

    private void setConnectedState(boolean connected) {
        btnConnect.setEnabled(!connected);
        btnDisconnect.setEnabled(connected);
        btnAdd.setEnabled(connected);
        btnUpdate.setEnabled(connected);
        btnDelete.setEnabled(connected);
        btnView.setEnabled(connected);
        lblStatus.setText(connected ? "Connected to " + HOST + ":" + PORT : "Not connected.");
    }

    private void connect() {
        setConnectedState(false);
        lblStatus.setText("Connecting...");
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                try {
                    socket = new Socket(HOST, PORT);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    return "Connected to server.";
                } catch (IOException ex) {
                    return "ERROR: " + ex.getMessage();
                }
            }
            protected void done() {
                try {
                    String res = get();
                    if (res.startsWith("ERROR:")) {
                        lblStatus.setText(res);
                        setConnectedState(false);
                    } else {
                        lblStatus.setText(res);
                        setConnectedState(true);
                        viewAll(); // load initial data
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    lblStatus.setText("ERROR: " + ex.getMessage());
                    setConnectedState(false);
                }
            }
        }.execute();
    }

    private void disconnect() {
        if (out != null) {
            try {
                out.println("EXIT");
            } catch (Exception ignored) { }
        }
        closeSocket();
        setConnectedState(false);
        lblStatus.setText("Disconnected.");
    }

    private void closeSocket() {
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (Exception ignored) {}
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (IOException ignored) {}
        in = null; out = null; socket = null;
    }

    // send command and call callback with response (on EDT)
    private void sendCommandAsync(String cmd, java.util.function.Consumer<String> callback) {
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                try {
                    if (out == null || in == null || socket == null || socket.isClosed()) {
                        return "ERROR: Not connected to server.";
                    }
                    out.println(cmd);
                    String resp = in.readLine();
                    if (resp == null) return "ERROR: No response (server closed connection).";
                    return resp;
                } catch (IOException ex) {
                    return "ERROR: " + ex.getMessage();
                }
            }
            protected void done() {
                try { callback.accept(get()); }
                catch (InterruptedException | ExecutionException e) { callback.accept("ERROR: " + e.getMessage()); }
            }
        }.execute();
    }

    private void addStudent() {
        String roll = txtRoll.getText().trim();
        String name = txtName.getText().trim();
        String degree = txtDegree.getText().trim();
        String sem = txtSemester.getText().trim();

        if (roll.isEmpty() || name.isEmpty()) {
            lblStatus.setText("Roll No and Name are required.");
            return;
        }
        if (!roll.matches("\\d+")) {
            lblStatus.setText("Roll No must be numeric.");
            return;
        }

        String cmd = String.join("|", "ADD", roll, name, degree, sem);
        sendCommandAsync(cmd, response -> {
            lblStatus.setText(response);
            if (response.startsWith("SUCCESS")) viewAll();
        });
    }

    private void updateStudent() {
        String roll = txtRoll.getText().trim();
        String name = txtName.getText().trim();
        String degree = txtDegree.getText().trim();
        String sem = txtSemester.getText().trim();

        if (roll.isEmpty()) {
            lblStatus.setText("Roll No is required to update.");
            return;
        }
        if (!roll.matches("\\d+")) {
            lblStatus.setText("Roll No must be numeric.");
            return;
        }

        String cmd = String.join("|", "UPDATE", roll, name, degree, sem);
        sendCommandAsync(cmd, response -> {
            lblStatus.setText(response);
            if (response.startsWith("SUCCESS")) viewAll();
        });
    }

    private void deleteStudent() {
        String roll = txtRoll.getText().trim();
        if (roll.isEmpty()) {
            lblStatus.setText("Enter Roll No to delete.");
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "Delete student with roll " + roll + " ?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;

        String cmd = "DELETE|" + roll;
        sendCommandAsync(cmd, response -> {
            lblStatus.setText(response);
            if (response.startsWith("SUCCESS")) viewAll();
        });
    }

    private void viewAll() {
        sendCommandAsync("VIEW", response -> {
            if (response == null) { lblStatus.setText("No response"); return; }
            if (!response.startsWith("SUCCESS:")) {
                lblStatus.setText(response);
                return;
            }
            String payload = response.substring("SUCCESS:".length()).trim();
            tableModel.setRowCount(0);
            if (!payload.isEmpty()) {
                String[] rows = payload.split(";");
                for (String row : rows) {
                    String[] cols = row.split(",", -1);
                    // ensure 4 columns
                    String r = cols.length > 0 ? cols[0] : "";
                    String name = cols.length > 1 ? cols[1] : "";
                    String degree = cols.length > 2 ? cols[2] : "";
                    String sem = cols.length > 3 ? cols[3] : "";
                    tableModel.addRow(new Object[] { r, name, degree, sem });
                }
            }
            lblStatus.setText("View completed. " + tableModel.getRowCount() + " rows.");
        });
    }

    private void onTableSelect(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int r = table.getSelectedRow();
            if (r >= 0) {
                txtRoll.setText(tableModel.getValueAt(r, 0).toString());
                txtName.setText(tableModel.getValueAt(r, 1).toString());
                txtDegree.setText(tableModel.getValueAt(r, 2).toString());
                txtSemester.setText(tableModel.getValueAt(r, 3).toString());
            }
        }
    }

    public static void main(String[] args) {
        // Set look and feel quickly
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
