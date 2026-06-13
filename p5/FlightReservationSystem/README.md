# ✈ Flight Reservation System

A beginner-friendly **Java OOP** project with a pure HTML/CSS/JavaScript frontend.  
Built without Spring Boot, no database — all data is stored in memory using `ArrayList`.

> Suitable for BTech 2nd Semester Java OOP Project

---

## 📁 Project Structure

```
FlightReservationSystem/
├── backend/
│   ├── Passenger.java      # OOP model — name, id, getters/setters, toString
│   ├── Flight.java         # Business logic — book, cancel, search, display
│   └── Main.java           # HTTP server + API route handlers
└── frontend/
    ├── index.html          # Dashboard UI
    ├── style.css           # Responsive card layout, blue buttons
    └── script.js           # fetch() calls to Java backend
```

---

## 🛠 Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Frontend | HTML5, CSS3, Vanilla JavaScript     |
| Backend  | Core Java (JDK 11+)                 |
| Server   | `com.sun.net.httpserver` (built-in) |
| Storage  | In-memory `ArrayList<Passenger>`    |

No Spring Boot. No database. No external libraries.

---

## ✅ Features

- 🪑 View available and total seat count (live, color-coded)
- 📋 Book a seat by entering Passenger Name and ID
- ❌ Cancel a reservation by Passenger ID
- 🔍 Search for a passenger by ID
- 👥 View the full list of booked passengers
- 🚫 Prevents duplicate Passenger IDs
- ⚠ Input validation on both frontend and backend
- 💬 Success and error messages with 5-second auto-dismiss
- 📱 Responsive layout — works on mobile and desktop

---

## ⚙ Prerequisites

- **Java JDK 11 or higher** installed
- Any modern web browser (Chrome, Edge, Firefox)

Check your Java version:
```bash
java -version
```

---

## 🚀 How to Run

### Step 1 — Compile the Backend

Open a terminal and navigate to the `backend` folder:

```bash
cd FlightReservationSystem/backend
```

Compile all three Java files:

```bash
javac Passenger.java Flight.java Main.java
```

You should see no errors — three `.class` files will be created.

---

### Step 2 — Start the Server

Still inside the `backend` folder, run:

```bash
java Main
```

Expected output:
```
==============================================
  Flight Reservation System Server Started!
  Listening on http://localhost:8080
  Total seats: 10
==============================================
```

Keep this terminal open while using the app.

---

### Step 3 — Open the Frontend

Open `FlightReservationSystem/frontend/index.html` in your browser.

- **Windows:** Double-click `index.html`, or right-click → Open With → Browser
- **Mac/Linux:** `open frontend/index.html` or drag into browser

The dashboard will load and automatically fetch seat availability and the passenger list.

---

## 🌐 API Endpoints

The Java backend exposes these REST endpoints on `http://localhost:8080`:

| Method   | Endpoint          | Description                  | Request Body               |
|----------|-------------------|------------------------------|----------------------------|
| `GET`    | `/seats`          | Get available & total seats  | —                          |
| `GET`    | `/passengers`     | Get all booked passengers    | —                          |
| `POST`   | `/book`           | Book a seat                  | `{"name":"Anas","id":101}` |
| `DELETE` | `/cancel/101`     | Cancel booking by ID         | —                          |
| `GET`    | `/search/101`     | Search passenger by ID       | —                          |

### Sample Responses

**GET /passengers**
```json
[
  { "name": "Anas", "id": 101 },
  { "name": "Sara", "id": 102 }
]
```

**POST /book** (success)
```json
{ "message": "SUCCESS: Seat booked for Anas (ID: 101)." }
```

**POST /book** (duplicate)
```json
{ "message": "ERROR: Passenger with ID 101 is already booked." }
```

**DELETE /cancel/101**
```json
{ "message": "SUCCESS: Booking cancelled for passenger ID 101." }
```

**GET /search/101**
```json
{ "name": "Anas", "id": 101 }
```

---

## 🏗 OOP Design

### `Passenger.java`
Demonstrates **encapsulation** — all fields are `private`, accessed via getters and setters.

```
Fields   : private String name | private int id
Methods  : Constructor, getName(), setName(), getId(), setId(), toString()
```

### `Flight.java`
Demonstrates **data management** using `ArrayList` as in-memory storage.

```
Fields   : private final int MAX_SEATS | private ArrayList<Passenger> passengers
Methods  : bookSeat()  cancelSeat()  searchPassenger()  displayPassengers()  availableSeats()
```

### `Main.java`
Demonstrates **inner classes** — each API route is handled by a dedicated inner class implementing `HttpHandler`.

```
Handlers : PassengersHandler  BookHandler  CancelHandler  SearchHandler  SeatsHandler
Helpers  : sendResponse()  readBody()  passengersToJson()  extractJsonString()  extractJsonInt()
```

---

## 🖥 UI Overview

```
┌─────────────────────────────────────────┐
│  ✈  Flight Reservation System           │  ← Header
├─────────────────────────────────────────┤
│  🪑 Seat Availability:  8 / 10          │  ← Seat card (full width)
├───────────────────┬─────────────────────┤
│  📋 Book a Seat   │  👥 Booked          │
│  [Name input    ] │  Passengers List    │
│  [ID input      ] │  • Anas — ID: 101  │
│  [Book Seat btn ] │  • Sara — ID: 102  │
├───────────────────┼─────────────────────┤
│  🔍 Search        │  ❌ Cancel Booking  │
│  [ID input      ] │  [ID input        ] │
│  [Search btn    ] │  [Cancel btn      ] │
└───────────────────┴─────────────────────┘
```

---

## 🎨 UI Design Highlights

- White card layout with soft box shadows
- Blue (`#1e88e5`) primary buttons with hover effects
- Red danger button for cancel
- Available seat count changes color:
  - 🔵 Blue — seats available
  - 🟠 Orange — 2 or fewer seats left
  - 🔴 Red — flight is full
- Fully responsive — stacks to single column on screens under 600px
- Smooth `fadeIn` animation on passenger list items

---

## 🔒 Validation & Error Handling

| Scenario                        | Handled By          | Message                                      |
|---------------------------------|---------------------|----------------------------------------------|
| Empty passenger name            | Frontend + Backend  | "Passenger name cannot be empty."            |
| Invalid / negative ID           | Frontend + Backend  | "Please enter a valid positive Passenger ID."|
| Duplicate Passenger ID          | Backend             | "Passenger with ID X is already booked."     |
| Flight full (10 seats)          | Backend             | "No seats available. Flight is full."        |
| Passenger not found (search)    | Backend             | "No passenger found with ID X."              |
| Passenger not found (cancel)    | Backend             | "No passenger found with ID X."              |
| Server not running              | Frontend            | "Cannot connect to server."                  |

---

## 📌 Notes

- The server runs on **port 8080** by default. If that port is busy, change `8080` in `Main.java` line:
  ```java
  HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
  ```
- Total seats default to **10**. Change it in `Main.java`:
  ```java
  private static Flight flight = new Flight(10);
  ```
- All data is **lost when the server is stopped** (in-memory only — by design for this project).

---

## 👨‍💻 Author

**BTech 2nd Semester — Java OOP Project**  
Flight Reservation System © 2025
