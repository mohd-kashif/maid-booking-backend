package com.rupeek.maidbooking.payment.domain;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(PaymentId id);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
