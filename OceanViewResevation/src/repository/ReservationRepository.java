package repository;

import model.Reservation;
import util.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReservationRepository {

    private DatabaseConnection db = DatabaseConnection.getInstance();

    public int save(Reservation res) throws SQLException {
        String sql = "INSERT INTO reservations (" +
                "guest_name, address, contact_number, room_type, " +
                "check_in, check_out) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, res.getGuestName());
            pstmt.setString(2, res.getAddress());
            pstmt.setString(3, res.getContactNumber());
            pstmt.setString(4, res.getRoomType());
            pstmt.setString(5, res.getCheckInDate());
            pstmt.setString(6, res.getCheckOutDate());

            pstmt.executeUpdate();

            // Get the auto-generated reservation_number
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedNumber = generatedKeys.getInt(1);
                    res.setReservationNumber(generatedNumber);
                    return generatedNumber;
                }
            }
        }
        return 0; // fallback
    }

    // New method: Fetch reservation by number
    public Reservation findByNumber(int resNumber) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE reservation_number = ?";

        try (PreparedStatement pstmt = db.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, resNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Reservation res = new Reservation();
                    res.setReservationNumber(rs.getInt("reservation_number"));
                    res.setGuestName(rs.getString("guest_name"));
                    res.setAddress(rs.getString("address"));
                    res.setContactNumber(rs.getString("contact_number"));
                    res.setRoomType(rs.getString("room_type"));
                    res.setCheckInDate(rs.getString("check_in"));
                    res.setCheckOutDate(rs.getString("check_out"));
                    res.calculateCost(); // recalculate if needed
                    return res;
                }
            }
        }
        return null; // not found
    }
}