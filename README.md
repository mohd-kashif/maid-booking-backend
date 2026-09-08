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
