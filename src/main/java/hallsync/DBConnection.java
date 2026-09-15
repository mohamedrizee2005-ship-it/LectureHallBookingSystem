package hallsync;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Database connection parameters for LectureHallBookingDB
    private static final String URL = "jdbc:mysql://localhost:3306/LectureHallBookingDB?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "YOUR_MYSQL_PASSWORD"; // <-- Replace with your MySQL root password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Check pom.xml dependencies.", e);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
