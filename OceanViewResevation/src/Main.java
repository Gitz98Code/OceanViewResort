import model.Reservation;
import service.ReservationService;
import java.util.Scanner;
import java.text.SimpleDateFormat;

public class Main {

    private static final ReservationService service = new ReservationService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static int nextResNumber = 1001;

    public static void main(String[] args) {
        System.out.println("=====================================");
        System.out.println("   Welcome to Ocean View Resort");
        System.out.println("      Room Reservation System");
        System.out.println("=====================================");

        WebServer.start(); // Starts HTTP server on port 8080
        boolean loggedIn = false;

        while (true) {
            System.out.println("\nMain Menu:");

            if (loggedIn) {
                System.out.println("1. Logout");
                System.out.println("2. Add New Reservation");
                System.out.println("3. Display Reservation Details");
                System.out.println("4. Calculate and Print Bill");
                System.out.println("5. Help Section");
                System.out.println("6. Exit System");
            } else {
                System.out.println("1. Login (Staff only)");
                System.out.println("6. Exit System");
                System.out.println("(Other options are locked until you login)");
            }

            System.out.print("\nEnter your choice: ");
            int choice = getIntInput();

            if (!loggedIn && choice >= 2 && choice <= 5) {
                System.out.println("Please login first to access this feature.");
                continue;
            }

            if (loggedIn) {
                if (choice == 1) {
                    loggedIn = false;
                    System.out.println("You have been logged out.");
                } else if (choice == 2) {
                    addNewReservation();
                } else if (choice == 3) {
                    displayReservation();
                } else if (choice == 4) {
                    printBill();
                } else if (choice == 5) {
                    showHelp();
                } else if (choice == 6) {
                    System.out.println("\nThank you! Goodbye.");
                    break;
                } else {
                    System.out.println("Invalid choice! Please enter 1-6.");
                }
            } else {
                if (choice == 1) {
                    login();
                    loggedIn = true;
                } else if (choice == 6) {
                    System.out.println("\nThank you! Goodbye.");
                    break;
                } else {
                    System.out.println("Please login first to access other features.");
                }
            }
        }

        scanner.close();
    }

    private static void login() {
        System.out.print("Username: ");
        String user = scanner.nextLine().trim();

        System.out.print("Password: ");
        String pass = scanner.nextLine().trim();

        if ("admin".equals(user) && "pass123".equals(pass)) {
            System.out.println("Login successful! Welcome.");
        } else {
            System.out.println("Login failed! Wrong credentials.");
        }
    }

    private static void addNewReservation() {
        System.out.println("\n=== Add New Reservation ===");

        Reservation res = new Reservation();

        System.out.print("Guest Full Name: ");
        res.setGuestName(scanner.nextLine().trim());

        System.out.print("Address: ");
        res.setAddress(scanner.nextLine().trim());

        System.out.print("Contact Number (exactly 10 digits): ");
        res.setContactNumber(scanner.nextLine().trim());

        System.out.print("Room Type (Standard / Deluxe / Suite): ");
        res.setRoomType(scanner.nextLine().trim());

        System.out.print("Check-in Date (YYYY-MM-DD): ");
        res.setCheckInDate(scanner.nextLine().trim());

        System.out.print("Check-out Date (YYYY-MM-DD): ");
        res.setCheckOutDate(scanner.nextLine().trim());

        // Assign number, calculate cost, save
        res.setReservationNumber(nextResNumber);
        res.calculateCost();
        service.addReservation(res);

        System.out.println("\nReservation added successfully!");
        System.out.println("Assigned Reservation Number: " + nextResNumber);
        res.printDetails();

        nextResNumber++;
    }

    private static void displayReservation() {
        System.out.println("\n=== Display Reservation Details ===");
        System.out.print("Enter Reservation Number: ");
        int num = getIntInput();

        Reservation found = service.findByNumber(num);

        if (found != null) {
            System.out.println("\nReservation Found:");
            found.printDetails();
        } else {
            System.out.println("No reservation found with number: " + num);
        }
    }

    private static void printBill() {
        System.out.println("\n=== Calculate and Print Bill ===");
        System.out.print("Enter Reservation Number: ");
        int num = getIntInput();

        Reservation found = service.findByNumber(num);

        if (found == null) {
            System.out.println("No reservation found.");
            return;
        }

        found.calculateCost(); // ensure latest cost
        System.out.println("\n=====================================");
        System.out.println("       OCEAN VIEW RESORT BILL");
        System.out.println("=====================================");
        System.out.println("Reservation Number: " + found.getReservationNumber());
        System.out.println("Guest Name        : " + found.getGuestName());
        System.out.println("Room Type         : " + found.getRoomType());
        System.out.println("Check-in Date     : " + found.getCheckInDate());
        System.out.println("Check-out Date    : " + found.getCheckOutDate());
        System.out.printf("Total Amount      : $%.2f%n", found.getTotalCost());
        System.out.println("=====================================");
        System.out.println("Thank you for staying with us!");
    }

    private static void showHelp() {
        System.out.println("\nHelp Section:");
        System.out.println(" - Login with admin");
        System.out.println(" - Add reservations with valid dates and 10-digit contact");
        System.out.println(" - All data is saved to the database");
        System.out.println(" - Use option 6 to exit safely");
    }

    private static int getIntInput() {
        while (!scanner.hasNextInt()) {
            System.out.print("Enter a number: ");
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // clear newline
        return value;
    }
}