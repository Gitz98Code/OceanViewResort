package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Reservation {
    private int reservationNumber;
    private String guestName;
    private String address;
    private String contactNumber;
    private String roomType;
    private String checkInDate; // "yyyy-MM-dd"
    private String checkOutDate;
    private double totalCost; // calculated

    // Constructor
    public Reservation(int resNumber, String name, String addr, String contact, String type, String inDate,
            String outDate) {
        this.reservationNumber = resNumber;
        this.guestName = name;
        this.address = addr;
        this.contactNumber = contact;
        this.roomType = type;
        this.checkInDate = inDate;
        this.checkOutDate = outDate;
        calculateCost(); // auto-calculate on creation
    }

    // Empty constructor (for DB fetch later)
    public Reservation() {
    }

    // Getters & Setters
    public int getReservationNumber() {
        return reservationNumber;
    }

    public void setReservationNumber(int reservationNumber) {
        this.reservationNumber = reservationNumber;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    // Calculate cost
    public void calculateCost() {
        if (checkInDate == null || checkOutDate == null) {
            totalCost = 0;
            return;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate in = LocalDate.parse(checkInDate, formatter);
            LocalDate out = LocalDate.parse(checkOutDate, formatter);

            long nights = ChronoUnit.DAYS.between(in, out);

            if (nights <= 0) {
                totalCost = 0;
                return;
            }

            double rate;
            String type = roomType.toLowerCase();
            if (type.contains("standard"))
                rate = 100;
            else if (type.contains("deluxe"))
                rate = 150;
            else
                rate = 200; // suite or default

            totalCost = nights * rate;
        } catch (Exception e) {
            totalCost = 0;
        }
    }

    // Print details
    public void printDetails() {
        System.out.println("Reservation Number: " + reservationNumber);
        System.out.println("Guest Name: " + guestName);
        System.out.println("Address: " + address);
        System.out.println("Contact: " + contactNumber);
        System.out.println("Room Type: " + roomType);
        System.out.println("Check-in: " + checkInDate);
        System.out.println("Check-out: " + checkOutDate);
        System.out.printf("Total Cost: $%.2f%n", totalCost);
    }

    @Override
    public String toString() {
        return "Reservation #" + reservationNumber + "\n" +
                "Guest: " + guestName + "\n" +
                "Room: " + roomType + "\n" +
                "Check-in: " + checkInDate + "\n" +
                "Check-out: " + checkOutDate + "\n" +
                "Total: $" + String.format("%.2f", totalCost);
    }
}