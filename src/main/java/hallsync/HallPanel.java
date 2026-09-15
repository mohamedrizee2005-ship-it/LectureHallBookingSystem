package hallsync;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HallPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;

    public HallPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UIUtils.BG);
        setOpaque(false);

        JPanel card = UIUtils.card();
        card.setLayout(new BorderLayout(0, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("University Lecture Halls & Installed Facilities");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(UIUtils.NAVY);

        JButton refresh = UIUtils.button("Refresh List", UIUtils.BLUE);
        refresh.addActionListener(e -> loadHalls());

        header.add(title, BorderLayout.WEST);
        header.add(refresh, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(new String[]{"Hall ID", "Hall No", "Capacity", "Location", "Status", "Installed Facilities"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        UIUtils.styleTable(table);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER));
        card.add(scrollPane, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);
        loadHalls();
    }

    private void loadHalls() {
        model.setRowCount(0);
        String sql = "SELECT h.HallID, h.HallNo, h.Capacity, h.Location, h.Status, " +
                     "IFNULL(GROUP_CONCAT(CONCAT(f.FacilityName, ' (', hf.Quantity, ')') SEPARATOR ', '), 'None') AS Facilities " +
                     "FROM Lecture_Hall h " +
                     "LEFT JOIN Hall_Facility hf ON h.HallID = hf.HallID " +
                     "LEFT JOIN Facility f ON hf.FacilityID = f.FacilityID " +
                     "GROUP BY h.HallID, h.HallNo, h.Capacity, h.Location, h.Status ORDER BY h.HallNo";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("HallID"), rs.getString("HallNo"), rs.getInt("Capacity"),
                    rs.getString("Location"), rs.getString("Status"), rs.getString("Facilities")
                });
            }
        } catch (SQLException ex) {
            UIUtils.error(this, ex.getMessage());
        }
    }
}