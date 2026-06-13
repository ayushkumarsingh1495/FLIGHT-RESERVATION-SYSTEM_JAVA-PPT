/**
 * Passenger.java
 * Represents a passenger with a name and ID.
 * Demonstrates OOP concepts: encapsulation, constructors, getters/setters.
 */
public class Passenger {

    // Private fields - encapsulation
    private String name;
    private int id;

    // Constructor - initializes a new Passenger object
    public Passenger(String name, int id) {
        this.name = name;
        this.id = id;
    }

    // Getter for name
    public String getName() {
        return name;
    }

    // Setter for name
    public void setName(String name) {
        this.name = name;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Setter for id
    public void setId(int id) {
        this.id = id;
    }

    // toString() - returns a readable representation of the Passenger
    @Override
    public String toString() {
        return "Passenger{name='" + name + "', id=" + id + "}";
    }
}
