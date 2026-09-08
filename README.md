# Maid Booking Backend

Spring Boot backend for the Rupeek maid booking machine-coding assignment.

## Requirements

- Java 17 or newer
- Maven 3.9 or newer (or a compatible Maven wrapper supplied by the IDE)

## Build and run

```bash
mvn clean test
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

## Project structure

The application uses a Maven build and is intentionally starting with a small,
framework-ready foundation. Business code will be organised by responsibility
under `com.rupeek.maidbooking`, keeping domain logic separate from REST and
in-memory persistence adapters as the implementation grows.

## Current scope

This initial setup provides:

- Java 17 and Spring Boot configuration
- Web and Bean Validation dependencies for the REST layer
- A smoke test that verifies the Spring application context loads
- Maven and IDE build artifacts excluded from version control

The next implementation step is the domain model for maids, services,
availability windows, and bookings.

## Maid onboarding API

The first module currently provides:

- `POST /api/maids` to register an active maid.
- `GET /api/maids/{maidId}` to retrieve a maid profile.
- `POST /api/maids/{maidId}/availability` to add a weekly availability window.
- `GET /api/maids/{maidId}/availability?day=MONDAY&start=10:00&end=12:00` to check availability.

Example registration request:

```json
{
  "name": "Asha",
  "locality": "Indiranagar",
  "services": ["CLEANING", "DISHWASHING"],
  "price": 500,
  "currency": "INR",
  "availability": [
    { "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "13:00" }
  ]
}
```

Availability is modelled as recurring weekly windows. Booking-specific occupied
slots will be layered on top of this in the booking module. Persistence is
currently in memory behind [`MaidRepository`](src/main/java/com/rupeek/maidbooking/maid/domain/MaidRepository.java).
