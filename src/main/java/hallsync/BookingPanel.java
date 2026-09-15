package hallsync;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class BookingPanel extends JPanel {

    private final int userID;
    private final String role;

    private JComboBox<String> hallCombo;
    private JTextField dateField;
    private JTextField startTimeField;
    private JComboBox<String> startAmPmCombo;
    private JTextField endTimeField;
    private JComboBox<String> endAmPmCombo;
    private JTextField studentCountField;
    private JTextField purposeField;
    private JTextField searchField;
    private JLabel totalBookingsBadge;

    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    public BookingPanel(int userID, String role) {
        this.userID = userID;
        this.role = role;

        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setOpaque(false);

        add(createBookingFormCard(), BorderLayout.NORTH);
        add(createTableCard(), BorderLayout.CENTER);

        loadHalls();
        loadBookings();
    }

    // Modern Form Card with 3 Neat Columns
    private JPanel createBookingFormCard() {
        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout(0, 14));

        // Form Title & Subtitle
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        headerPanel.setOpaque(false);
        JLabel titleLbl = new JLabel("Reserve a Lecture Hall");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(UIUtils.NAVY);

        JLabel subLbl = new JLabel("Select hall, specify date and time range, and verify live availability.");
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(UIUtils.TEXT_MUTED);

        headerPanel.add(titleLbl);
        headerPanel.add(subLbl);
        card.add(headerPanel, BorderLayout.NORTH);

        // 3-Column Grid with labels stacked directly on top of inputs
        JPanel fieldsGrid = new JPanel(new GridLayout(2, 3, 16, 12));
        fieldsGrid.setOpaque(false);

        // Col 1, Row 1: Hall
        JPanel hallGroup = createFieldGroup("LECTURE HALL", hallCombo = new JComboBox<>());

        // Col 2, Row 1: Date
        dateField = new JTextField("2026-09-10");
        UIUtils.styleInputComponent(dateField);
        JPanel dateGroup = createFieldGroup("BOOKING DATE (YYYY-MM-DD)", dateField);

        // Col 3, Row 1: Student Count
        studentCountField = new JTextField();
        UIUtils.styleInputComponent(studentCountField);
        JPanel studentGroup = createFieldGroup("EXPECTED ATTENDEES", studentCountField);

        // Col 1, Row 2: Start Time
        JPanel startTimeBox = new JPanel(new BorderLayout(6, 0));
        startTimeBox.setOpaque(false);
        startTimeField = new JTextField("09:00", 6);
        UIUtils.styleInputComponent(startTimeField);
        startAmPmCombo = new JComboBox<>(new String[]{"AM", "PM"});
        startTimeBox.add(startTimeField, BorderLayout.CENTER);
        startTimeBox.add(startAmPmCombo, BorderLayout.EAST);
        JPanel startGroup = createFieldGroup("START TIME", startTimeBox);

        // Col 2, Row 2: End Time
        JPanel endTimeBox = new JPanel(new BorderLayout(6, 0));
        endTimeBox.setOpaque(false);
        endTimeField = new JTextField("11:00", 6);
        UIUtils.styleInputComponent(endTimeField);
        endAmPmCombo = new JComboBox<>(new String[]{"AM", "PM"});
        endTimeBox.add(endTimeField, BorderLayout.CENTER);
        endTimeBox.add(endAmPmCombo, BorderLayout.EAST);
        JPanel endGroup = createFieldGroup("END TIME", endTimeBox);

        // Col 3, Row 2: Purpose
        purposeField = new JTextField();
        UIUtils.styleInputComponent(purposeField);
        JPanel purposeGroup = createFieldGroup("PURPOSE / MODULE NAME", purposeField);

        fieldsGrid.add(hallGroup);
        fieldsGrid.add(dateGroup);
        fieldsGrid.add(studentGroup);
        fieldsGrid.add(startGroup);
        fieldsGrid.add(endGroup);
        fieldsGrid.add(purposeGroup);

        card.add(fieldsGrid, BorderLayout.CENTER);

        // Action Buttons Row with Clear Hierarchy
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(6, 0, 0, 0));

        JPanel leftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftActions.setOpaque(false);
        JButton checkBtn = UIUtils.button("Check Availability", UIUtils.BLUE);
        JButton bookBtn = UIUtils.button("+ Book Lecture Hall", UIUtils.EMERALD);
        leftActions.add(checkBtn);
        leftActions.add(bookBtn);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);
        JButton updateBtn = UIUtils.button("Update Selected", UIUtils.AMBER);
        JButton cancelBtn = UIUtils.button("Cancel Booking", UIUtils.RED);
        rightActions.add(updateBtn);
        rightActions.add(cancelBtn);

        bottomRow.add(leftActions, BorderLayout.WEST);
        bottomRow.add(rightActions, BorderLayout.EAST);
        card.add(bottomRow, BorderLayout.SOUTH);

        checkBtn.addActionListener(e -> checkAvailability());
        bookBtn.addActionListener(e -> bookHall());
        updateBtn.addActionListener(e -> updateBooking());
        cancelBtn.addActionListener(e -> cancelBooking());

        return card;
    }

    // Modern Table Card with Integrated Search Header
    private JPanel createTableCard() {
        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JPanel leftTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftTitle.setOpaque(false);
        JLabel title = new JLabel("Reservation History");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(UIUtils.NAVY);

        totalBookingsBadge = new JLabel(" 0 records ");
        totalBookingsBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        totalBookingsBadge.setOpaque(true);
        totalBookingsBadge.setBackground(new Color(241, 245, 249));
        totalBookingsBadge.setForeground(UIUtils.TEXT_MUTED);
        totalBookingsBadge.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER, 1, true));

        leftTitle.add(title);
        leftTitle.add(totalBookingsBadge);

        // Clean Search Bar
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchIcon.setForeground(UIUtils.TEXT_MUTED);

        searchField = new JTextField(18);
        UIUtils.styleInputComponent(searchField);

        searchBox.add(searchIcon);
        searchBox.add(searchField);

        tableHeader.add(leftTitle, BorderLayout.WEST);
        tableHeader.add(searchBox, BorderLayout.EAST);
        card.add(tableHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Hall", "Date", "Start Time", "End Time", "Purpose", "Status", "Students"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        bookingTable = new JTable(tableModel);
        UIUtils.styleTable(bookingTable);

        rowSorter = new TableRowSorter<>(tableModel);
        bookingTable.setRowSorter(rowSorter);

        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        bookingTable.getSelectionModel().addListSelectionListener(e -> selectBooking());

        return card;
    }

    private JPanel createFieldGroup(String labelText, JComponent component) {
        JPanel group = new JPanel(new BorderLayout(0, 5));
        group.setOpaque(false);

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 116, 139)); // Slate-500

        group.add(lbl, BorderLayout.NORTH);
        group.add(component, BorderLayout.CENTER);
        return group;
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    private void loadHalls() {
        hallCombo.removeAllItems();
        String sql = "SELECT HallID, HallNo, Capacity, Location FROM Lecture_Hall WHERE Status <> 'Maintenance' ORDER BY HallNo";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                hallCombo.addItem(rs.getInt("HallID") + " - " + rs.getString("HallNo") + " (" + rs.getString("Location") + ")");
            }
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private int getSelectedHallID() {
        String selected = (String) hallCombo.getSelectedItem();
        if (selected == null) return -1;
        return Integer.parseInt(selected.split(" - ")[0]);
    }

    private boolean validateInput() {
        if (dateField.getText().trim().isEmpty() || startTimeField.getText().trim().isEmpty()
                || endTimeField.getText().trim().isEmpty() || studentCountField.getText().trim().isEmpty()
                || purposeField.getText().trim().isEmpty()) {
            UIUtils.error(this, "Please fill in all booking fields.");
            return false;
        }
        try {
            int students = Integer.parseInt(studentCountField.getText().trim());
            if (students <= 0) {
                UIUtils.error(this, "Student count must be greater than 0.");
                return false;
            }
        } catch (NumberFormatException ex) {
            UIUtils.error(this, "Student count must be a valid integer number.");
            return false;
        }
        return true;
    }

    private Time parseTime(String timeStr, String amPm) throws Exception {
        String[] parts = timeStr.trim().split(":");
        if (parts.length != 2) throw new IllegalArgumentException("Time must be HH:MM format.");
        int hour = Integer.parseInt(parts[0].trim());
        int minute = Integer.parseInt(parts[1].trim());

        if (hour < 1 || hour > 12 || minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid time range. Hours: 1-12, Minutes: 0-59.");
        }

        if (amPm.equalsIgnoreCase("PM") && hour < 12) hour += 12;
        else if (amPm.equalsIgnoreCase("AM") && hour == 12) hour = 0;

        return Time.valueOf(String.format("%02d:%02d:00", hour, minute));
    }

    private void checkAvailability() {
        if (!validateInput()) return;
        int hallID = getSelectedHallID();

        try {
            Time startTime = parseTime(startTimeField.getText(), (String) startAmPmCombo.getSelectedItem());
            Time endTime = parseTime(endTimeField.getText(), (String) endAmPmCombo.getSelectedItem());

            String sql = "SELECT COUNT(*) AS count FROM Booking WHERE HallID = ? AND BookingDate = ? AND BookingStatus IN ('Pending','Approved') AND StartTime < ? AND EndTime > ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setInt(1, hallID);
                pst.setDate(2, Date.valueOf(dateField.getText().trim()));
                pst.setTime(3, startTime);
                pst.setTime(4, endTime);

                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt("count") == 0) {
                    UIUtils.info(this, "Lecture hall is AVAILABLE for this timeslot.");
                } else {
                    UIUtils.error(this, "Lecture hall is already BOOKED for this timeslot.");
                }
            }
        } catch (Exception ex) {
            UIUtils.error(this, "Time format error: " + ex.getMessage());
        }
    }

    private void bookHall() {
        if (!validateInput()) return;
        int hallID = getSelectedHallID();

        try {
            Time startTime = parseTime(startTimeField.getText(), (String) startAmPmCombo.getSelectedItem());
            Time endTime = parseTime(endTimeField.getText(), (String) endAmPmCombo.getSelectedItem());

            String insertSQL = "INSERT INTO Booking (UserID, HallID, BookingDate, StartTime, EndTime, Purpose, BookingStatus, RequestedStudentCount) VALUES (?, ?, ?, ?, ?, ?, 'Pending', ?)";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(insertSQL)) {

                pst.setInt(1, userID);
                pst.setInt(2, hallID);
                pst.setDate(3, Date.valueOf(dateField.getText().trim()));
                pst.setTime(4, startTime);
                pst.setTime(5, endTime);
                pst.setString(6, purposeField.getText().trim());
                pst.setInt(7, Integer.parseInt(studentCountField.getText().trim()));

                pst.executeUpdate();
                UIUtils.info(this, "Booking submitted successfully (Status: Pending)!");
                loadBookings();
            }
        } catch (Exception ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void loadBookings() {
        tableModel.setRowCount(0);
        String sql = "SELECT b.BookingID, h.HallNo, b.BookingDate, b.StartTime, b.EndTime, b.Purpose, b.BookingStatus, b.RequestedStudentCount " +
                     "FROM Booking b JOIN Lecture_Hall h ON b.HallID = h.HallID ORDER BY b.BookingDate DESC, b.StartTime ASC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            int count = 0;
            while (rs.next()) {
                count++;
                tableModel.addRow(new Object[]{
                    rs.getInt("BookingID"), rs.getString("HallNo"), rs.getDate("BookingDate"),
                    rs.getTime("StartTime"), rs.getTime("EndTime"), rs.getString("Purpose"),
                    rs.getString("BookingStatus"), rs.getInt("RequestedStudentCount")
                });
            }
            totalBookingsBadge.setText(" " + count + " records ");
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void selectBooking() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) return;
        int modelRow = bookingTable.convertRowIndexToModel(row);

        String hallNo = tableModel.getValueAt(modelRow, 1).toString();
        dateField.setText(tableModel.getValueAt(modelRow, 2).toString());
        setUiTime(tableModel.getValueAt(modelRow, 3).toString().substring(0, 5), startTimeField, startAmPmCombo);
        setUiTime(tableModel.getValueAt(modelRow, 4).toString().substring(0, 5), endTimeField, endAmPmCombo);
        purposeField.setText(tableModel.getValueAt(modelRow, 5).toString());
        studentCountField.setText(tableModel.getValueAt(modelRow, 7).toString());

        for (int i = 0; i < hallCombo.getItemCount(); i++) {
            if (hallCombo.getItemAt(i).contains(" - " + hallNo + " ")) {
                hallCombo.setSelectedIndex(i);
                break;
            }
        }
    }

    private void setUiTime(String time24, JTextField tf, JComboBox<String> amPmCombo) {
        String[] parts = time24.split(":");
        int hour = Integer.parseInt(parts[0]);
        String minute = parts[1];
        String amPm = "AM";

        if (hour >= 12) {
            amPm = "PM";
            if (hour > 12) hour -= 12;
        }
        if (hour == 0) hour = 12;

        tf.setText(String.format("%02d:%s", hour, minute));
        amPmCombo.setSelectedItem(amPm);
    }

    private int getSelectedBookingID() {
        int row = bookingTable.getSelectedRow();
        if (row == -1) return -1;
        int modelRow = bookingTable.convertRowIndexToModel(row);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    private void updateBooking() {
        int bookingID = getSelectedBookingID();
        if (bookingID == -1) {
            UIUtils.error(this, "Select a booking from the table first.");
            return;
        }
        if (!validateInput()) return;

        try {
            Time startTime = parseTime(startTimeField.getText(), (String) startAmPmCombo.getSelectedItem());
            Time endTime = parseTime(endTimeField.getText(), (String) endAmPmCombo.getSelectedItem());

            String sql = "UPDATE Booking SET HallID = ?, BookingDate = ?, StartTime = ?, EndTime = ?, Purpose = ?, RequestedStudentCount = ? WHERE BookingID = ?";

            try (Connection con = DBConnection.getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {

                pst.setInt(1, getSelectedHallID());
                pst.setDate(2, Date.valueOf(dateField.getText().trim()));
                pst.setTime(3, startTime);
                pst.setTime(4, endTime);
                pst.setString(5, purposeField.getText().trim());
                pst.setInt(6, Integer.parseInt(studentCountField.getText().trim()));
                pst.setInt(7, bookingID);

                pst.executeUpdate();
                UIUtils.info(this, "Booking updated successfully.");
                loadBookings();
            }
        } catch (Exception ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void cancelBooking() {
        int bookingID = getSelectedBookingID();
        if (bookingID == -1) {
            UIUtils.error(this, "Select a booking from the table first.");
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this booking?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE Booking SET BookingStatus = 'Cancelled' WHERE BookingID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, bookingID);
            pst.executeUpdate();
            UIUtils.info(this, "Booking has been cancelled.");
            loadBookings();
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }
}