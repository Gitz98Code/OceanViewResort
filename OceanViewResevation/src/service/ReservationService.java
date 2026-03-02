package service;

import model.Reservation;
import repository.ReservationRepository;

import java.sql.SQLException;

public class ReservationService {

    private ReservationRepository repository = new ReservationRepository();

    public int addReservation(Reservation res) {
        try {
            if (res.getGuestName() == null || res.getGuestName().trim().isEmpty()) {
                throw new IllegalArgumentException("Guest name is required");
            }

            return repository.save(res); // return the generated number
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return 0;
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
            return 0;
        }
    }

    // New method: Fetch by number
    public Reservation findByNumber(int number) {
        try {
            return repository.findByNumber(number);
        } catch (SQLException e) {
            System.out.println("Error fetching reservation: " + e.getMessage());
            return null;
        }
    }
}