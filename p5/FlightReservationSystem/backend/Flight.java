import java.util.ArrayList;

/**
 * Flight.java
 * Manages flight bookings using an ArrayList (in-memory storage).
 * Demonstrates OOP concepts: encapsulation, methods, ArrayList usage.
 */
public class Flight {

    // Maximum number of seats on the flight
    private final int MAX_SEATS;

    // In-memory list of booked passengers
    private ArrayList<Passenger> passengers;

    // Constructor - sets max seats and initializes the passenger list
    public Flight(int maxSeats) {
        this.MAX_SEATS = maxSeats;
        this.passengers = new ArrayList<>();
    }

    /**
     * Book a seat for a passenger.
     * Returns a message indicating success or the reason for failure.
     */
    public String bookSeat(String name, int id) {
        // Check if the flight is full
        if (passengers.size() >= MAX_SEATS) {
            return "ERROR: No seats available. Flight is full.";
        }

        // Check for duplicate passenger ID
        for (Passenger p : passengers) {
            if (p.getId() == id) {
                return "ERROR: Passenger with ID " + id + " is already booked.";
            }
        }

        // Validate name - must not be empty
        if (name == null || name.trim().isEmpty()) {
            return "ERROR: Passenger name cannot be empty.";
        }

        // Add the new passenger
        passengers.add(new Passenger(name.trim(), id));
        return "SUCCESS: Seat booked for " + name + " (ID: " + id + ").";
    }

    /**
     * Cancel a reservation by passenger ID.
     * Returns a message indicating success or failure.
     */
    public String cancelSeat(int id) {
        for (Passenger p : passengers) {
            if (p.getId() == id) {
                passengers.remove(p);
                return "SUCCESS: Booking cancelled for passenger ID " + id + ".";
            }
        }
        return "ERROR: No passenger found with ID " + id + ".";
    }

    /**
     * Search for a passenger by ID.
     * Returns the Passenger object if found, or null if not found.
     */
    public Passenger searchPassenger(int id) {
        for (Passenger p : passengers) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null; // Not found
    }

    /**
     * Returns the full list of booked passengers.
     */
    public ArrayList<Passenger> displayPassengers() {
        return passengers;
    }

    /**
     * Returns the number of available (unbooked) seats.
     */
    public int availableSeats() {
        return MAX_SEATS - passengers.size();
    }

    /**
     * Returns the total max seats.
     */
    public int getMaxSeats() {
        return MAX_SEATS;
    }
}
