package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static DatabaseConnection instance = null;
    private Connection connection = null;

    // === CHANGE THESE TO YOUR ACTUAL MySQL SETTINGS ===
    private static final String DB_URL = "jdbc:mysql://localhost:3306/oceanview?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root"; // your MySQL username
    private static final String PASS = "1234"; // your MySQL password

    private DatabaseConnection() {
        try {
            // Force load MySQL driver (very important for newer JDBC versions)
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL driver loaded successfully.");

            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            if (connection != null) {
                System.out.println("Connected to MySQL database successfully!");
                createTables();
            } else {
                System.err.println("Connection returned null!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL driver not found! Make sure mysql-connector-j-*.jar is in lib.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("MySQL connection failed: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        if (connection == null) {
            System.err.println("Warning: Connection is null - attempting to reconnect...");
            // Optional: try to reconnect if null
            try {
                connection = DriverManager.getConnection(DB_URL, USER, PASS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private void createTables() throws SQLException {
        if (connection == null) {
            throw new SQLException("No connection available to create tables");
        }

        String sql = "CREATE TABLE IF NOT EXISTS reservations (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "reservation_number INT UNIQUE NOT NULL, " +
                "guest_name VARCHAR(100) NOT NULL, " +
                "address VARCHAR(200), " +
                "contact_number VARCHAR(15), " +
                "room_type VARCHAR(50), " +
                "check_in DATE, " +
                "check_out DATE" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Reservations table is ready or already exists.");
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("MySQL connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}