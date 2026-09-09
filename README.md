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

The application is organised by business responsibility under
`com.rupeek.maidbooking`. Domain logic is kept separate from REST controllers,
application services, and in-memory persistence adapters.

## Implemented modules

- Maid onboarding with service offerings, pricing, gender, rating, and recurring availability.
- Extensible maid discovery filters for locality, services, price, rating, gender, and availability.
- Strategy-based instant, scheduled, and recurring bookings.
- Per-service pricing and immutable booking/payment price snapshots.
- Card, UPI, and wallet payments with idempotency support.
- Cancellation policies, recurring occurrence cancellation, and refunds.
- Consistent API error responses and OpenAPI documentation.
- In-memory repositories with active-slot overlap protection and concurrency tests.
- Unit and integration tests covering the main customer flow.

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
  "services": [
    { "type": "CLEANING", "price": 400, "currency": "INR" },
    { "type": "DISHWASHING", "price": 300, "currency": "INR" }
  ],
  "availability": [
    { "dayOfWeek": "MONDAY", "startTime": "09:00", "endTime": "13:00" }
  ]
}
```

Availability is modelled as recurring weekly windows. Booking-specific occupied
slots will be layered on top of this in the booking module. Persistence is
currently in memory behind [`MaidRepository`](src/main/java/com/rupeek/maidbooking/maid/domain/MaidRepository.java).

Each maid has one price per offered service. When a customer selects multiple
services, their prices are added together; for example, cleaning at ₹400 plus
dishwashing at ₹300 produces a booking subtotal of ₹700. All offerings for one
maid must currently use the same currency.

## Booking API

The booking module supports strategy-based instant, scheduled, and recurring
bookings:

- `POST /api/bookings` creates a booking.
- `GET /api/bookings/{bookingId}` retrieves a booking.

Example scheduled booking:

```json
{
  "customerId": "customer-1",
  "maidId": "maid-uuid",
  "type": "SCHEDULED",
  "services": ["CLEANING", "COOKING"],
  "start": "2026-09-15T10:00:00Z",
  "end": "2026-09-15T12:00:00Z"
}
```

Booking creation resolves a [`BookingStrategy`](src/main/java/com/rupeek/maidbooking/booking/application/BookingStrategy.java)
from a registry, so adding a new booking type does not require branching changes
inside the application service. The in-memory repository performs synchronized
slot reservation to prevent overlapping active bookings for the same maid.

## Payment API

The payment module supports strategy-based card, UPI, and wallet payments:

- `POST /api/payments` makes a payment for a confirmed booking.
- `GET /api/payments/{paymentId}` retrieves payment status and transaction details.

Example request:

```json
{
  "bookingId": "booking-uuid",
  "method": "UPI",
  "idempotencyKey": "customer-1-booking-uuid-v1",
  "paymentDetails": "customer@upi",
  "occurrenceIndex": null
}
```

Payment amount is copied from the booking price snapshot. Reusing the same
idempotency key returns the original payment and does not charge the gateway
again. [`MockPaymentProvider`](src/main/java/com/rupeek/maidbooking/payment/infrastructure/MockPaymentProvider.java)
implements both charge and refund capabilities, ensuring refunds use the same
provider abstraction as the original payment. Sending `fail` as payment
details simulates a gateway failure for local testing. Recurring bookings may
provide a zero-based `occurrenceIndex` to pay each occurrence independently.

## Cancellation and refund API

Bookings can be cancelled through the cancellation endpoint:

- `POST /api/bookings/{bookingId}/cancellations`

Cancel an entire booking:

```json
{
  "scope": "ENTIRE_BOOKING",
  "reason": "CUSTOMER_REQUEST",
  "policyType": "STANDARD"
}
```

Cancel one occurrence of a recurring booking:

```json
{
  "scope": "SINGLE_OCCURRENCE",
  "occurrenceIndex": 0,
  "reason": "CUSTOMER_REQUEST",
  "policyType": "STANDARD"
}
```

The current policy provides a full refund when cancellation happens more than
24 hours before the slot. Cancellations within 24 hours do not receive a
refund. The policy is behind [`CancellationPolicy`](src/main/java/com/rupeek/maidbooking/cancellation/application/CancellationPolicy.java)
and selected through [`CancellationPolicyRegistry`](src/main/java/com/rupeek/maidbooking/cancellation/application/CancellationPolicyRegistry.java)
so new policy implementations can be added without changing cancellation orchestration.

## Maid discovery API

Maid discovery is available through:

```text
GET /api/maids?locality=Indiranagar&services=CLEANING,COOKING&start=2026-09-15T10:00:00Z&end=2026-09-15T12:00:00Z&maxPrice=1200&minimumRating=4&gender=FEMALE
```

All query parameters are optional. When a time range is supplied, the result
includes only active maids whose configured weekly availability contains the
range and who have no overlapping active booking. Cancelled bookings and
cancelled recurring occurrences do not block discovery.

The search service composes [`MaidFilter`](src/main/java/com/rupeek/maidbooking/discovery/domain/MaidFilter.java)
implementations, so a new filter can be added as a Spring component without
changing the discovery orchestration.

## API documentation and errors

OpenAPI documentation is available when the service is running:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

All validation and domain errors use a consistent response shape:

```json
{
  "timestamp": "2026-09-09T12:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": {
    "services": "must not be empty"
  },
  "path": "/api/bookings"
}
```

The complete API flow is covered by [`ApiFlowIntegrationTest.java`](src/test/java/com/rupeek/maidbooking/ApiFlowIntegrationTest.java):
register a maid, discover her, create a booking, pay, verify the slot is hidden,
cancel the booking, verify the refund, and verify the maid is discoverable again.

## Postman collection

Import [`maid-booking.postman_collection.json`](postman/maid-booking.postman_collection.json)
from the `postman` directory into Postman. Start the application first, then run
the requests in this order:

1. `Maid / Register Maid`
2. `Discovery / Search Maids`
3. `Booking / Create Scheduled Booking`
4. `Payment / Pay With UPI`
5. `Cancellation / Cancel Booking`
6. `Cancellation / Search After Cancellation`

The collection stores the created maid, booking, and payment IDs in collection
variables automatically. Its pre-request script calculates the next Monday
booking slot dynamically.
