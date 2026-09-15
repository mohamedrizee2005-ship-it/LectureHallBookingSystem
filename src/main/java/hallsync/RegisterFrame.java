package hallsync;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class RegisterFrame extends JFrame {

    private JTextField nameField, emailField, usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> roleCombo;

    public RegisterFrame() {
        setTitle("HallSync - User Registration");
        setSize(500, 520);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(UIUtils.BG);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        JLabel title = new JLabel("CREATE NEW ACCOUNT", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(UIUtils.NAVY);
        mainPanel.add(title, BorderLayout.NORTH);

        JPanel formCard = UIUtils.card();
        formCard.setLayout(new GridLayout(6, 2, 10, 12));

        formCard.add(UIUtils.label("Full Name:"));
        nameField = new JTextField();
        UIUtils.styleInputComponent(nameField);
        formCard.add(nameField);

        formCard.add(UIUtils.label("Role:"));
        roleCombo = new JComboBox<>(new String[]{"Student", "Lecturer", "Non-Academic Staff"});
        formCard.add(roleCombo);

        formCard.add(UIUtils.label("Email:"));
        emailField = new JTextField();
        UIUtils.styleInputComponent(emailField);
        formCard.add(emailField);

        formCard.add(UIUtils.label("Username:"));
        usernameField = new JTextField();
        UIUtils.styleInputComponent(usernameField);
        formCard.add(usernameField);

        formCard.add(UIUtils.label("Password:"));
        passwordField = new JPasswordField();
        UIUtils.styleInputComponent(passwordField);
        formCard.add(passwordField);

        formCard.add(UIUtils.label("Confirm Password:"));
        confirmPasswordField = new JPasswordField();
        UIUtils.styleInputComponent(confirmPasswordField);
        formCard.add(confirmPasswordField);

        mainPanel.add(formCard, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        buttonPanel.setBackground(UIUtils.BG);

        JButton registerButton = UIUtils.button("REGISTER", UIUtils.EMERALD);
        JButton backButton = UIUtils.button("BACK TO LOGIN", UIUtils.RED);

        buttonPanel.add(registerButton);
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        registerButton.addActionListener(e -> registerUser());
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
    }

    private void registerUser() {
        String name = nameField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();
        String email = emailField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            UIUtils.error(this, "Please fill in all fields.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            UIUtils.error(this, "Passwords do not match. Please re-enter.");
            return;
        }

        String sql = "INSERT INTO `User` (Name, Role, Email, Username, Password) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, name);
            pst.setString(2, role);
            pst.setString(3, email);
            pst.setString(4, username);
            pst.setString(5, password);

            pst.executeUpdate();
            UIUtils.info(this, "Registration successful! You can now log in.");
            new LoginFrame().setVisible(true);
            dispose();

        } catch (SQLException ex) {
            UIUtils.error(this, "Registration Error (Username or Email may already exist):\n" + ex.getMessage());
        }
    }
}