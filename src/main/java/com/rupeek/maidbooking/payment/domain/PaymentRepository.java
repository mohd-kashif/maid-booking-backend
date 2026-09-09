package com.rupeek.maidbooking.payment.domain;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    Optional<Payment> findByBookingAndOccurrence(UUID bookingId, Integer occurrenceIndex);
    boolean existsSuccessfulPayment(UUID bookingId, Integer occurrenceIndex);
    Optional<Payment> findSuccessfulByBookingAndOccurrence(UUID bookingId, Integer occurrenceIndex);
}
