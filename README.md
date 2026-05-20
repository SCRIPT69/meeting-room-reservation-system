# Meeting Room Reservation System

A Spring Boot REST application for managing meeting room reservations with a strong focus on software testing and business-rule validation.

The project demonstrates practical testing techniques including unit testing, Mockito-based isolation testing, integration testing, equivalence partitioning, boundary value analysis (BVA), and pairwise testing.

## About the Project

The system allows users to create and cancel room reservations through a REST API. During reservation creation, the application validates multiple business constraints and automatically assigns reservation statuses based on the number of attendees.

The project was designed to explore how business rules can be systematically validated using multiple software testing approaches.

**Tech stack:** Java, Spring Boot, JUnit 5, Mockito, Maven, in-memory repositories

---

## Business Rules

Reservations are validated against the following rules:

* Reservations are only allowed on **weekdays (Mon–Fri)**
* Reservations must fall within **working hours (08:00–18:00)**
* Start and end times must be aligned to **15-minute intervals**
* Minimum duration: **30 minutes**, maximum: **240 minutes**
* Number of attendees must be **greater than 0** and must not exceed room capacity
* Reservations must not conflict with existing reservations in the same room

### Automatic Status Assignment

| Attendees  | Status               |
| ---------- | -------------------- |
| 1–4        | `CONFIRMED`          |
| 5–capacity | `PENDING`            |
| > capacity | Reservation rejected |

---

## API Endpoints

| Method  | Endpoint                    | Description                    |
| ------- | --------------------------- | ------------------------------ |
| `POST`  | `/reservations`             | Create a new reservation       |
| `PATCH` | `/reservations/{id}/cancel` | Cancel an existing reservation |

### Example Request

```json
{
  "roomId": 1,
  "people": 3,
  "start": "2026-03-16T10:00:00",
  "end": "2026-03-16T11:00:00"
}
```

### Example Response

```json
{
  "id": 1,
  "roomId": 1,
  "people": 3,
  "start": "2026-03-16T10:00:00",
  "end": "2026-03-16T11:00:00",
  "status": "CONFIRMED"
}
```

---

## Project Structure

```text
src/
├── main/java/.../
│   ├── controller/        # REST API layer
│   ├── service/           # Business logic & validation
│   ├── repository/        # In-memory data storage
│   └── model/             # Reservation, Room, ReservationStatus
└── test/java/.../
    ├── service/
    │   ├── ReservationServiceTest.java
    │   └── ReservationServiceMockitoTest.java
    └── ReservationIntegrationTest.java
```

---

## Testing

The project demonstrates multiple testing levels and test design techniques.

### Unit Tests

Focused on isolated validation of business rules in `validateReservation()`:

* invalid time intervals
* invalid 15-minute alignment
* capacity violations
* weekend reservations
* reservations outside working hours
* too short / too long reservations
* valid reservation scenarios

### Mockito-Based Tests

Mockito is used to isolate service-layer logic from repository dependencies.

The tests cover:

* automatic status assignment
* conflict detection
* missing room handling
* reservation confirmation and cancellation
* repository interaction verification using `ArgumentCaptor`

### Integration Tests

Spring Boot integration tests validate collaboration between controllers, services, and repositories using the full application context.

Covered scenarios include:

* successful reservation creation
* reservation persistence
* conflict detection
* cancellation flow
* validation errors
* re-booking after cancellation

---

## Test Design Techniques

The testing strategy is supported by formal test analysis methods:

* **Equivalence Classes (EC)**
* **Boundary Value Analysis (BVA)**
* **Pairwise Testing** using ACTS (2-way coverage)
* **Detailed Test Scenarios** with preconditions, steps, and expected results

Key parameters analyzed include:

* attendee count
* reservation duration
* working hours
* day of week
* time alignment
* reservation conflicts

---

## How to Run

### Clone the Repository

```bash
git clone https://github.com/SCRIPT69/meeting-room-reservation-system.git
cd meeting-room-reservation-system
```

### Run All Tests

```bash
./mvnw test
```

### Start the Application

```bash
./mvnw spring-boot:run
```

---

## Purpose of the Project

This project was created as a practical exercise in software testing and validation of business logic in REST applications.

The primary goal was not only to implement reservation functionality, but also to demonstrate structured testing techniques and systematic validation of edge cases and input constraints.
