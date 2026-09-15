package hallsync;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("HallSync - Lecture Hall Booking System");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(UIUtils.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 45, 30, 45));

        // Fixed header without the broken emoji box
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        JLabel title = new JLabel("HALLSYNC PORTAL", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(UIUtils.NAVY);

        JLabel subtitle = new JLabel("Department of Industrial Management", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(UIUtils.TEXT_MUTED);

        topPanel.add(title);
        topPanel.add(subtitle);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel formCard = UIUtils.card();
        formCard.setLayout(new GridLayout(3, 2, 12, 16));

        formCard.add(UIUtils.label("Username:"));
        usernameField = new JTextField();
        UIUtils.styleInputComponent(usernameField);
        formCard.add(usernameField);

        formCard.add(UIUtils.label("Password:"));
        passwordField = new JPasswordField();
        UIUtils.styleInputComponent(passwordField);
        formCard.add(passwordField);

        JButton loginButton = UIUtils.button("LOGIN", UIUtils.EMERALD);
        JButton exitButton = UIUtils.button("EXIT", UIUtils.RED);

        formCard.add(loginButton);
        formCard.add(exitButton);

        mainPanel.add(formCard, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(UIUtils.BG);
        JButton registerScreenBtn = new JButton("Don't have an account? Register here");
        registerScreenBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        registerScreenBtn.setForeground(UIUtils.BLUE);
        registerScreenBtn.setBorderPainted(false);
        registerScreenBtn.setContentAreaFilled(false);
        registerScreenBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerScreenBtn.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        bottomPanel.add(registerScreenBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        };

        usernameField.addKeyListener(enterKeyListener);
        passwordField.addKeyListener(enterKeyListener);

        loginButton.addActionListener(e -> login());
        exitButton.addActionListener(e -> System.exit(0));
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            UIUtils.error(this, "Please enter both username and password.");
            return;
        }

        String sql = "SELECT UserID, Name, Role FROM `User` WHERE Username = ? AND Password = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, username);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                int userID = rs.getInt("UserID");
                String name = rs.getString("Name");
                String role = rs.getString("Role");

                // Successful login -> open Dashboard
                new DashboardFrame(userID, name, role).setVisible(true);
                dispose();
            } else {
                UIUtils.error(this, "Invalid credentials!\nUsername or password does not match any record.");
            }

        } catch (SQLException ex) {
            UIUtils.error(this, "Database Connection Failed!\n\nDetails: " + ex.getMessage() + 
                                "\n\nPlease check:\n1. Is MySQL Server running?\n2. Is your password correct in DBConnection.java?");
        }
    }
}