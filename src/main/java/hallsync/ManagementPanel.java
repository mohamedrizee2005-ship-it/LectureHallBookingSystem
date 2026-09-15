package hallsync;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ManagementPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private final String userRole;

    public ManagementPanel(String userRole) {
        this.userRole = userRole;
        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.BG);

        JLabel title = new JLabel("BOOKING MANAGEMENT & APPROVALS", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(UIUtils.NAVY);
        add(title, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"Booking ID", "User Name", "Role", "Hall", "Date", "Start", "End", "Purpose", "Status", "Students"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(model);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(UIUtils.BG);

        JButton approveBtn = UIUtils.button("APPROVE SELECTED", UIUtils.EMERALD);
        JButton rejectBtn = UIUtils.button("REJECT / CANCEL", UIUtils.RED);
        JButton refreshBtn = UIUtils.button("REFRESH TABLE", UIUtils.BLUE);

        if ("Student".equalsIgnoreCase(userRole)) {
            approveBtn.setEnabled(false);
            rejectBtn.setEnabled(false);
        }

        buttonPanel.add(approveBtn);
        buttonPanel.add(rejectBtn);
        buttonPanel.add(refreshBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        approveBtn.addActionListener(e -> updateStatus("Approved"));
        rejectBtn.addActionListener(e -> updateStatus("Cancelled"));
        refreshBtn.addActionListener(e -> loadAllBookings());

        loadAllBookings();
    }

    private void loadAllBookings() {
        model.setRowCount(0);
        String sql = "SELECT b.BookingID, u.Name AS UserName, u.Role, h.HallNo, b.BookingDate, " +
                     "b.StartTime, b.EndTime, b.Purpose, b.BookingStatus, b.RequestedStudentCount " +
                     "FROM Booking b " +
                     "JOIN `User` u ON b.UserID = u.UserID " +
                     "JOIN Lecture_Hall h ON b.HallID = h.HallID " +
                     "ORDER BY b.BookingDate, b.StartTime";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("BookingID"),
                    rs.getString("UserName"),
                    rs.getString("Role"),
                    rs.getString("HallNo"),
                    rs.getDate("BookingDate"),
                    rs.getTime("StartTime"),
                    rs.getTime("EndTime"),
                    rs.getString("Purpose"),
                    rs.getString("BookingStatus"),
                    rs.getInt("RequestedStudentCount")
                });
            }
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void updateStatus(String newStatus) {
        int row = table.getSelectedRow();
        if (row == -1) {
            UIUtils.error(this, "Please select a booking from the table first.");
            return;
        }

        int bookingID = (int) model.getValueAt(row, 0);
        String sql = "UPDATE Booking SET BookingStatus = ? WHERE BookingID = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, newStatus);
            pst.setInt(2, bookingID);
            pst.executeUpdate();

            UIUtils.info(this, "Booking status updated to: " + newStatus);
            loadAllBookings();

        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }
}