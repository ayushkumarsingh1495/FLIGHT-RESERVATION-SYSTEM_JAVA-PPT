/**
 * script.js — Flight Reservation System Frontend
 * Uses fetch() to communicate with the Java backend at localhost:8080.
 *
 * Functions:
 *   bookSeat()        — POST /book
 *   cancelSeat()      — DELETE /cancel/{id}
 *   searchPassenger() — GET /search/{id}
 *   loadPassengers()  — GET /passengers
 *   loadSeats()       — GET /seats
 */

// Base URL of the Java backend server
const BASE_URL = "http://localhost:8000";

// ─────────────────────────────────────────────────────────────
// bookSeat()
// Reads the name and ID inputs, sends a POST request to /book
// ─────────────────────────────────────────────────────────────
async function bookSeat() {
  const nameInput = document.getElementById("passengerName");
  const idInput   = document.getElementById("passengerId");
  const msgBox    = document.getElementById("bookMessage");

  const name = nameInput.value.trim();
  const id   = parseInt(idInput.value.trim());

  // --- Frontend validation ---
  if (!name) {
    showMessage(msgBox, "Please enter a passenger name.", "error");
    return;
  }
  if (!idInput.value.trim() || isNaN(id) || id <= 0) {
    showMessage(msgBox, "Please enter a valid positive Passenger ID.", "error");
    return;
  }

  try {
    const response = await fetch(`${BASE_URL}/book`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name: name, id: id })
    });

    const data = await response.json();

    if (response.ok && data.message.startsWith("SUCCESS")) {
      showMessage(msgBox, data.message, "success");
      // Clear inputs on success
      nameInput.value = "";
      idInput.value   = "";
      // Refresh seat count and passenger list automatically
      loadSeats();
      loadPassengers();
    } else {
      showMessage(msgBox, data.message || "Booking failed.", "error");
    }
  } catch (err) {
    showMessage(msgBox, "Cannot connect to server. Is the Java backend running?", "error");
  }
}

// ─────────────────────────────────────────────────────────────
// cancelSeat()
// Reads the cancel ID input, sends DELETE /cancel/{id}
// ─────────────────────────────────────────────────────────────
async function cancelSeat() {
  const idInput = document.getElementById("cancelId");
  const msgBox  = document.getElementById("cancelMessage");

  const id = parseInt(idInput.value.trim());

  // --- Frontend validation ---
  if (!idInput.value.trim() || isNaN(id) || id <= 0) {
    showMessage(msgBox, "Please enter a valid positive Passenger ID.", "error");
    return;
  }

  try {
    const response = await fetch(`${BASE_URL}/cancel/${id}`, {
      method: "DELETE"
    });

    const data = await response.json();

    if (response.ok && data.message.startsWith("SUCCESS")) {
      showMessage(msgBox, data.message, "success");
      idInput.value = "";
      // Refresh seat count and passenger list automatically
      loadSeats();
      loadPassengers();
    } else {
      showMessage(msgBox, data.message || "Cancellation failed.", "error");
    }
  } catch (err) {
    showMessage(msgBox, "Cannot connect to server. Is the Java backend running?", "error");
  }
}

// ─────────────────────────────────────────────────────────────
// searchPassenger()
// Reads the search ID input, sends GET /search/{id}
// ─────────────────────────────────────────────────────────────
async function searchPassenger() {
  const idInput  = document.getElementById("searchId");
  const resultBox = document.getElementById("searchResult");

  const id = parseInt(idInput.value.trim());

  // --- Frontend validation ---
  if (!idInput.value.trim() || isNaN(id) || id <= 0) {
    showMessage(resultBox, "Please enter a valid positive Passenger ID.", "error");
    return;
  }

  try {
    const response = await fetch(`${BASE_URL}/search/${id}`);
    const data     = await response.json();

    if (response.ok && data.name) {
      // Passenger found — display their details
      showMessage(resultBox, `✅ Found: ${data.name} (ID: ${data.id})`, "success");
    } else {
      showMessage(resultBox, data.message || "Passenger not found.", "error");
    }
  } catch (err) {
    showMessage(resultBox, "Cannot connect to server. Is the Java backend running?", "error");
  }
}

// ─────────────────────────────────────────────────────────────
// loadPassengers()
// Sends GET /passengers and renders the list
// ─────────────────────────────────────────────────────────────
async function loadPassengers() {
  const listDiv = document.getElementById("passengerList");

  try {
    const response  = await fetch(`${BASE_URL}/passengers`);
    const passengers = await response.json();

    // Clear current list
    listDiv.innerHTML = "";

    if (passengers.length === 0) {
      listDiv.innerHTML = '<p class="empty-msg">No passengers booked yet.</p>';
      return;
    }

    // Build one card per passenger
    passengers.forEach((p, index) => {
      const item = document.createElement("div");
      item.className = "passenger-item";
      item.innerHTML = `
        <div>
          <span class="p-name">${escapeHtml(p.name)}</span>
          <span class="p-id"> — ID: ${p.id}</span>
        </div>
        <span style="color:#aaa; font-size:0.8rem;">#${index + 1}</span>
      `;
      listDiv.appendChild(item);
    });

  } catch (err) {
    listDiv.innerHTML = '<p class="empty-msg" style="color:#e53935;">Cannot connect to server.</p>';
  }
}

// ─────────────────────────────────────────────────────────────
// loadSeats()
// Sends GET /seats and updates the seat availability display
// ─────────────────────────────────────────────────────────────
async function loadSeats() {
  try {
    const response = await fetch(`${BASE_URL}/seats`);
    const data     = await response.json();

    document.getElementById("availableSeats").textContent = data.available;
    document.getElementById("totalSeats").textContent     = data.total;

    // Turn the available count red when flight is almost full (≤ 2 seats left)
    const countEl = document.getElementById("availableSeats");
    if (data.available === 0) {
      countEl.style.color = "#e53935"; // red — full
    } else if (data.available <= 2) {
      countEl.style.color = "#fb8c00"; // orange — almost full
    } else {
      countEl.style.color = "#1565c0"; // blue — normal
    }

  } catch (err) {
    document.getElementById("availableSeats").textContent = "?";
    document.getElementById("totalSeats").textContent     = "?";
  }
}

// ─────────────────────────────────────────────────────────────
// showMessage(element, text, type)
// Displays a styled message ("success", "error", or "info")
// inside the given element.
// ─────────────────────────────────────────────────────────────
function showMessage(element, text, type) {
  element.textContent  = text;
  element.className    = `message ${type}`; // applies CSS class
  element.style.display = "block";

  // Auto-hide after 5 seconds
  setTimeout(() => {
    element.style.display = "none";
  }, 5000);
}

// ─────────────────────────────────────────────────────────────
// escapeHtml(str)
// Prevents XSS by escaping user-supplied strings before
// inserting them into innerHTML.
// ─────────────────────────────────────────────────────────────
function escapeHtml(str) {
  const div = document.createElement("div");
  div.appendChild(document.createTextNode(str));
  return div.innerHTML;
}

// ─────────────────────────────────────────────────────────────
// On page load — fetch seat count and passenger list right away
// ─────────────────────────────────────────────────────────────
window.addEventListener("DOMContentLoaded", () => {
  loadSeats();
  loadPassengers();
});
