package hallsync;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardFrame extends JFrame {

    private final int userID;
    private final String userName;
    private final String role;

    private CardLayout cardLayout;
    private JPanel mainContentArea;
    private List<JButton> navButtons = new ArrayList<>();

    private JLabel pendingMetricLabel;
    private JLabel hallsMetricLabel;
    private JLabel usersMetricLabel;

    public DashboardFrame(int userID, String userName, String role) {
        this.userID = userID;
        this.userName = userName;
        this.role = role;

        setTitle("HallSync - University Lecture Hall Management");
        setSize(1340, 840);
        setMinimumSize(new Dimension(1120, 720));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
        loadKPIMetrics();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG);

        // 1. Sidebar on the left
        JPanel sidebar = createSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // 2. Right workspace
        JPanel rightArea = new JPanel(new BorderLayout());
        rightArea.setBackground(UIUtils.BG);

        JPanel headerPanel = createHeaderPanel();
        rightArea.add(headerPanel, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContentArea = new JPanel(cardLayout);
        mainContentArea.setBackground(UIUtils.BG);
        mainContentArea.setBorder(new EmptyBorder(8, 24, 24, 24));

        mainContentArea.add(new BookingPanel(userID, role), "BOOK");
        mainContentArea.add(new HallPanel(), "HALLS");

        if ("Non-Academic Staff".equalsIgnoreCase(role)) {
            mainContentArea.add(new ManagementPanel(role), "APPROVALS");
        }
        mainContentArea.add(new ReportPanel(), "REPORTS");

        rightArea.add(mainContentArea, BorderLayout.CENTER);
        root.add(rightArea, BorderLayout.CENTER);

        add(root);
        setActiveNav(navButtons.get(0), "BOOK");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(UIUtils.NAVY);
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Brand Banner with vector icon (NO EMOJIS)
        JPanel brandPanel = new JPanel(new BorderLayout(12, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("H", SwingConstants.CENTER);
        logoIcon.setPreferredSize(new Dimension(38, 38));
        logoIcon.setOpaque(true);
        logoIcon.setBackground(UIUtils.EMERALD);
        logoIcon.setForeground(Color.WHITE);
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 18));

        JPanel brandText = new JPanel(new GridLayout(2, 1, 0, 2));
        brandText.setOpaque(false);
        JLabel brandName = new JLabel("HallSync");
        brandName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brandName.setForeground(Color.WHITE);

        JLabel deptName = new JLabel("Dept. of Ind. Mgmt");
        deptName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        deptName.setForeground(new Color(148, 163, 184));

        brandText.add(brandName);
        brandText.add(deptName);

        brandPanel.add(logoIcon, BorderLayout.WEST);
        brandPanel.add(brandText, BorderLayout.CENTER);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        // Navigation Menu
        JPanel navContainer = new JPanel();
        navContainer.setLayout(new BoxLayout(navContainer, BoxLayout.Y_AXIS));
        navContainer.setOpaque(false);
        navContainer.setBorder(new EmptyBorder(32, 0, 0, 0));

        JButton btnBook = createNavButton("Book Hall", "BOOK");
        JButton btnHalls = createNavButton("Lecture Halls", "HALLS");
        navContainer.add(btnBook);
        navContainer.add(Box.createVerticalStrut(6));
        navContainer.add(btnHalls);
        navContainer.add(Box.createVerticalStrut(6));

        if ("Non-Academic Staff".equalsIgnoreCase(role)) {
            JButton btnApprovals = createNavButton("Manage Approvals", "APPROVALS");
            navContainer.add(btnApprovals);
            navContainer.add(Box.createVerticalStrut(6));
        }

        JButton btnReports = createNavButton("Reports & Export", "REPORTS");
        navContainer.add(btnReports);

        sidebar.add(navContainer, BorderLayout.CENTER);

        // Bottom User Profile Card
        JPanel userBottomCard = new JPanel(new BorderLayout(10, 0));
        userBottomCard.setOpaque(false);
        userBottomCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)),
            new EmptyBorder(16, 0, 0, 0)
        ));

        String initial = userName.isEmpty() ? "U" : userName.substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER);
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setOpaque(true);
        avatar.setBackground(UIUtils.BLUE);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel userMeta = new JPanel(new GridLayout(2, 1));
        userMeta.setOpaque(false);
        JLabel nameLbl = new JLabel(userName);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLbl.setForeground(Color.WHITE);

        JLabel roleLbl = new JLabel(role);
        roleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        roleLbl.setForeground(new Color(148, 163, 184));
        userMeta.add(nameLbl);
        userMeta.add(roleLbl);

        JButton logoutBtn = UIUtils.button("Exit", UIUtils.RED);
        logoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        logoutBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
        logoutBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        userBottomCard.add(avatar, BorderLayout.WEST);
        userBottomCard.add(userMeta, BorderLayout.CENTER);
        userBottomCard.add(logoutBtn, BorderLayout.EAST);

        sidebar.add(userBottomCard, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String title, String cardName) {
        JButton btn = new JButton(title);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(208, 40));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(UIUtils.NAVY);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(0, 16, 0, 0));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.getBackground() != UIUtils.NAVY_ACTIVE) {
                    btn.setBackground(UIUtils.NAVY2);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.getBackground() != UIUtils.NAVY_ACTIVE) {
                    btn.setBackground(UIUtils.NAVY);
                }
            }
        });

        btn.addActionListener(e -> setActiveNav(btn, cardName));
        navButtons.add(btn);
        return btn;
    }

    private void setActiveNav(JButton activeBtn, String cardName) {
        for (JButton b : navButtons) {
            b.setBackground(UIUtils.NAVY);
            b.setForeground(new Color(203, 213, 225));
            b.setBorder(new EmptyBorder(0, 16, 0, 0));
        }
        activeBtn.setBackground(UIUtils.NAVY_ACTIVE);
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, UIUtils.EMERALD),
            new EmptyBorder(0, 12, 0, 0)
        ));
        cardLayout.show(mainContentArea, cardName);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BG);
        header.setBorder(new EmptyBorder(18, 24, 10, 24));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);

        JLabel welcomeTitle = new JLabel("Welcome back, " + userName);
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeTitle.setForeground(UIUtils.TEXT);

        JLabel subTitle = new JLabel("Group 08 – OOP Lecture Hall Booking System");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(UIUtils.TEXT_MUTED);

        titleBox.add(welcomeTitle);
        titleBox.add(subTitle);

        // 3 KPI Metric Cards
        JPanel statsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        statsRow.setOpaque(false);

        pendingMetricLabel = new JLabel("...", SwingConstants.CENTER);
        hallsMetricLabel = new JLabel("...", SwingConstants.CENTER);
        usersMetricLabel = new JLabel("...", SwingConstants.CENTER);

        statsRow.add(createMiniKPICard("Pending Requests", pendingMetricLabel, UIUtils.AMBER));
        statsRow.add(createMiniKPICard("Available Halls", hallsMetricLabel, UIUtils.EMERALD));
        statsRow.add(createMiniKPICard("Registered Users", usersMetricLabel, UIUtils.BLUE));

        header.add(titleBox, BorderLayout.WEST);
        header.add(statsRow, BorderLayout.EAST);
        return header;
    }

    private JPanel createMiniKPICard(String label, JLabel valLbl, Color accent) {
        JPanel card = new JPanel(new BorderLayout(6, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER, 1),
                new EmptyBorder(8, 14, 8, 14)
            )
        ));

        JLabel tLbl = new JLabel(label.toUpperCase());
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tLbl.setForeground(UIUtils.TEXT_MUTED);

        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valLbl.setForeground(UIUtils.TEXT);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    public void loadKPIMetrics() {
        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            ResultSet rs1 = st.executeQuery("SELECT COUNT(*) FROM Booking WHERE BookingStatus = 'Pending'");
            if (rs1.next()) pendingMetricLabel.setText(rs1.getInt(1) + " Pending");

            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) FROM Lecture_Hall WHERE Status = 'Available'");
            if (rs2.next()) hallsMetricLabel.setText(rs2.getInt(1) + " Available");

            ResultSet rs3 = st.executeQuery("SELECT COUNT(*) FROM `User`");
            if (rs3.next()) usersMetricLabel.setText(rs3.getInt(1) + " Users");

        } catch (SQLException ex) {
            pendingMetricLabel.setText("-");
            hallsMetricLabel.setText("-");
            usersMetricLabel.setText("-");
        }
    }
}