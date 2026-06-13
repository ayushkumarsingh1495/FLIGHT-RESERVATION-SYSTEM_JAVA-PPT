import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Main.java
 * Starts a lightweight HTTP server using Java's built-in com.sun.net.httpserver.
 * Handles API requests and connects the frontend with the backend logic.
 *
 * API Endpoints:
 *   GET    /passengers       - Get all booked passengers
 *   POST   /book             - Book a seat  (body: {"name":"Anas","id":101})
 *   DELETE /cancel/{id}      - Cancel booking by passenger ID
 *   GET    /search/{id}      - Search passenger by ID
 *   GET    /seats            - Get available seat count
 */
public class Main {

    // One shared Flight instance — acts as our in-memory "database"
    private static Flight flight = new Flight(10); // 10 total seats

    public static void main(String[] args) throws IOException {

        // Create HTTP server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Register route handlers
        server.createContext("/passengers", new PassengersHandler());
        server.createContext("/book",       new BookHandler());
        server.createContext("/cancel",     new CancelHandler());
        server.createContext("/search",     new SearchHandler());
        server.createContext("/seats",      new SeatsHandler());

        // Use default executor (creates threads as needed)
        server.setExecutor(null);
        server.start();

        System.out.println("==============================================");
        System.out.println("  Flight Reservation System Server Started!");
        System.out.println("  Listening on http://localhost:8000");
        System.out.println("  Total seats: 10");
        System.out.println("==============================================");
    }

    // -----------------------------------------------------------------------
    // Helper: send a JSON response with the given status code and body
    // -----------------------------------------------------------------------
    private static void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        // Allow requests from the frontend (CORS)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Content-Type", "application/json");

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    // -----------------------------------------------------------------------
    // Helper: read request body as a String
    // -----------------------------------------------------------------------
    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // -----------------------------------------------------------------------
    // Helper: convert the passenger list to a JSON array string
    // -----------------------------------------------------------------------
    private static String passengersToJson(ArrayList<Passenger> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Passenger p = list.get(i);
            sb.append("{\"name\":\"").append(p.getName())
              .append("\",\"id\":").append(p.getId()).append("}");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Helper: handle browser pre-flight OPTIONS requests (CORS)
    // -----------------------------------------------------------------------
    private static boolean handleOptions(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return true;
        }
        return false;
    }

    // ===========================
    // Handler: GET /passengers
    // Returns all booked passengers as JSON array
    // ===========================
    static class PassengersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String json = passengersToJson(flight.displayPassengers());
            sendResponse(exchange, 200, json);
        }
    }

    // ===========================
    // Handler: POST /book
    // Body: {"name":"Anas","id":101}
    // ===========================
    static class BookHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String body = readBody(exchange);

            // Simple JSON parsing (no external library needed)
            String name = extractJsonString(body, "name");
            int id = extractJsonInt(body, "id");

            if (name == null || id == -1) {
                sendResponse(exchange, 400, "{\"message\":\"ERROR: Invalid request body. Provide name and id.\"}");
                return;
            }

            String result = flight.bookSeat(name, id);
            boolean success = result.startsWith("SUCCESS");
            int status = success ? 200 : 400;
            sendResponse(exchange, status, "{\"message\":\"" + result + "\"}");
        }
    }

    // ===========================
    // Handler: DELETE /cancel/{id}
    // ===========================
    static class CancelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            // Extract ID from URL path, e.g. /cancel/101 -> 101
            String path = exchange.getRequestURI().getPath(); // "/cancel/101"
            String[] parts = path.split("/");

            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"message\":\"ERROR: Passenger ID missing in URL.\"}");
                return;
            }

            try {
                int id = Integer.parseInt(parts[2]);
                String result = flight.cancelSeat(id);
                boolean success = result.startsWith("SUCCESS");
                int status = success ? 200 : 404;
                sendResponse(exchange, status, "{\"message\":\"" + result + "\"}");
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"message\":\"ERROR: Invalid ID format.\"}");
            }
        }
    }

    // ===========================
    // Handler: GET /search/{id}
    // ===========================
    static class SearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath(); // "/search/101"
            String[] parts = path.split("/");

            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"message\":\"ERROR: Passenger ID missing in URL.\"}");
                return;
            }

            try {
                int id = Integer.parseInt(parts[2]);
                Passenger p = flight.searchPassenger(id);

                if (p != null) {
                    String json = "{\"name\":\"" + p.getName() + "\",\"id\":" + p.getId() + "}";
                    sendResponse(exchange, 200, json);
                } else {
                    sendResponse(exchange, 404, "{\"message\":\"ERROR: No passenger found with ID " + id + ".\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"message\":\"ERROR: Invalid ID format.\"}");
            }
        }
    }

    // ===========================
    // Handler: GET /seats
    // Returns available seat count
    // ===========================
    static class SeatsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleOptions(exchange)) return;

            String json = "{\"available\":" + flight.availableSeats()
                        + ",\"total\":" + flight.getMaxSeats() + "}";
            sendResponse(exchange, 200, json);
        }
    }

    // -----------------------------------------------------------------------
    // Simple JSON value extractor — avoids needing external JSON libraries
    // Extracts a string value: "key":"value"
    // -----------------------------------------------------------------------
    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return null;

        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx == -1) return null;

        int firstQuote = json.indexOf("\"", colonIdx);
        if (firstQuote == -1) return null;

        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;

        return json.substring(firstQuote + 1, secondQuote);
    }

    // Extracts an integer value: "key":123
    private static int extractJsonInt(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIdx = json.indexOf(search);
        if (keyIdx == -1) return -1;

        int colonIdx = json.indexOf(":", keyIdx);
        if (colonIdx == -1) return -1;

        // Read digits after the colon (skip spaces)
        StringBuilder digits = new StringBuilder();
        for (int i = colonIdx + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (digits.length() > 0) {
                break; // stop at first non-digit after digits started
            }
        }

        if (digits.length() == 0) return -1;

        try {
            return Integer.parseInt(digits.toString());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
