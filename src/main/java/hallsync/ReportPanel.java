package hallsync;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;

public class ReportPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public ReportPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setOpaque(false);

        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Audit & Booking Reports");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIUtils.NAVY);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = UIUtils.button("Refresh", UIUtils.BLUE);
        JButton exportCSV = UIUtils.button("Export CSV", UIUtils.ACCENT);

        actions.add(refresh);
        actions.add(exportCSV);

        header.add(title, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new String[]{"ID", "User", "Role", "Hall", "Date", "Start", "End", "Purpose", "Status", "Students"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        refresh.addActionListener(e -> generateReport());
        exportCSV.addActionListener(e -> exportToCSV());

        generateReport();
    }

    private void generateReport() {
        model.setRowCount(0);
        String sql = "SELECT b.BookingID, u.Name AS UserName, u.Role, h.HallNo, b.BookingDate, " +
                     "b.StartTime, b.EndTime, b.Purpose, b.BookingStatus, b.RequestedStudentCount " +
                     "FROM Booking b " +
                     "JOIN `User` u ON b.UserID = u.UserID " +
                     "JOIN Lecture_Hall h ON b.HallID = h.HallID " +
                     "ORDER BY b.BookingDate DESC, b.StartTime ASC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("BookingID"), rs.getString("UserName"), rs.getString("Role"),
                    rs.getString("HallNo"), rs.getDate("BookingDate"), rs.getTime("StartTime"),
                    rs.getTime("EndTime"), rs.getString("Purpose"), rs.getString("BookingStatus"),
                    rs.getInt("RequestedStudentCount")
                });
            }
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report as CSV");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter csvWriter = new FileWriter(fileToSave)) {
                for (int i = 0; i < model.getColumnCount(); i++) {
                    csvWriter.append(model.getColumnName(i));
                    if (i < model.getColumnCount() - 1) csvWriter.append(",");
                }
                csvWriter.append("\n");

                for (int i = 0; i < model.getRowCount(); i++) {
                    for (int j = 0; j < model.getColumnCount(); j++) {
                        csvWriter.append(model.getValueAt(i, j).toString());
                        if (j < model.getColumnCount() - 1) csvWriter.append(",");
                    }
                    csvWriter.append("\n");
                }

                UIUtils.info(this, "Report exported successfully to:\n" + fileToSave.getAbsolutePath());
            } catch (Exception ex) {
                UIUtils.error(this, "Export Error: " + ex.getMessage());
            }
        }
    }
}