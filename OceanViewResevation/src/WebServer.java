import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import model.Reservation;
import service.ReservationService;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServer {

    private static final ReservationService service = new ReservationService();
    private static final int PORT = 8080;

    // Path from OceanViewReservation → ../OceanView → frontend
    private static final Path FRONTEND_DIR = Paths.get("../OceanView/frontend").toAbsolutePath().normalize();

    public static void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/frontend/", new StaticFileHandler());

            server.createContext("/add-reservation", new AddReservationHandler());
            server.createContext("/reservation/", new GetReservationHandler());
            server.createContext("/bill/", new GetBillHandler());
            server.createContext("/login", new LoginHandler());

            server.setExecutor(null);
            server.start();

            System.out.println("Server running at http://localhost:" + PORT);
            System.out.println("Login page: http://localhost:" + PORT + "/frontend/index.html");
        } catch (IOException e) {
            System.err.println("Server failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestedPath = exchange.getRequestURI().getPath();
            String filePathStr = requestedPath.substring("/frontend".length());

            if (filePathStr.isEmpty() || filePathStr.equals("/")) {
                filePathStr = "/index.html";
            }

            Path filePath = FRONTEND_DIR.resolve(filePathStr.substring(1)).normalize();

            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                String notFound = "File not found: " + requestedPath;
                exchange.sendResponseHeaders(404, notFound.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(notFound.getBytes(StandardCharsets.UTF_8));
                }
                return;
            }

            String contentType = "text/html";
            if (filePathStr.endsWith(".css"))
                contentType = "text/css";
            if (filePathStr.endsWith(".js"))
                contentType = "application/javascript";
            if (filePathStr.endsWith(".jpg") || filePathStr.endsWith(".png") || filePathStr.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            }

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, Files.size(filePath));

            try (InputStream is = Files.newInputStream(filePath);
                    OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);

            try {
                JSONObject json = new JSONObject(body);
                String username = json.optString("username");
                String password = json.optString("password");

                if ("admin".equals(username) && "pass123".equals(password)) {
                    JSONObject response = new JSONObject();
                    response.put("status", "success");
                    response.put("message", "Login successful");
                    sendResponse(exchange, 200, response.toString());
                } else {
                    JSONObject response = new JSONObject();
                    response.put("status", "error");
                    response.put("message", "Invalid username or password");
                    sendResponse(exchange, 401, response.toString());
                }
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("status", "error");
                err.put("message", "Invalid request");
                sendResponse(exchange, 400, err.toString());
            }
        }
    }

    static class AddReservationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String body = readBody(exchange);

            try {
                JSONObject json = new JSONObject(body);

                Reservation res = new Reservation();
                res.setGuestName(json.optString("guestName"));
                res.setAddress(json.optString("address"));
                res.setContactNumber(json.optString("contactNumber"));
                res.setRoomType(json.optString("roomType"));
                res.setCheckInDate(json.optString("checkIn"));
                res.setCheckOutDate(json.optString("checkOut"));

                res.calculateCost();

                int newNumber = service.addReservation(res);

                JSONObject response = new JSONObject();
                response.put("status", "success");
                response.put("reservationNumber", newNumber);

                sendResponse(exchange, 201, response.toString());
            } catch (Exception e) {
                JSONObject err = new JSONObject();
                err.put("status", "error");
                err.put("message", e.getMessage());
                sendResponse(exchange, 400, err.toString());
            }
        }
    }

    static class GetReservationHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String numStr = path.substring("/reservation/".length()).trim();

            try {
                int number = Integer.parseInt(numStr);
                Reservation res = service.findByNumber(number);

                if (res != null) {
                    JSONObject json = new JSONObject();
                    json.put("reservationNumber", res.getReservationNumber());
                    json.put("guestName", res.getGuestName());
                    json.put("address", res.getAddress());
                    json.put("contactNumber", res.getContactNumber());
                    json.put("roomType", res.getRoomType());
                    json.put("checkIn", res.getCheckInDate());
                    json.put("checkOut", res.getCheckOutDate());
                    json.put("totalCost", res.getTotalCost());

                    sendResponse(exchange, 200, json.toString());
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Reservation not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid reservation number\"}");
            }
        }
    }

    static class GetBillHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String numStr = path.substring("/bill/".length()).trim();

            try {
                int number = Integer.parseInt(numStr);
                Reservation res = service.findByNumber(number);

                if (res != null) {
                    res.calculateCost();

                    JSONObject json = new JSONObject();
                    json.put("reservationNumber", res.getReservationNumber());
                    json.put("guestName", res.getGuestName());
                    json.put("roomType", res.getRoomType());
                    json.put("checkIn", res.getCheckInDate());
                    json.put("checkOut", res.getCheckOutDate());
                    json.put("totalAmount", res.getTotalCost());

                    sendResponse(exchange, 200, json.toString());
                } else {
                    sendResponse(exchange, 404, "{\"error\":\"Reservation not found\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid reservation number\"}");
            }
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private static void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.sendResponseHeaders(status, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }
}