package com.rupeek.maidbooking.payment.infrastructure;

import com.rupeek.maidbooking.payment.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPaymentRepository implements PaymentRepository {
    private final Map<PaymentId, Payment> payments = new ConcurrentHashMap<>();
    private final Map<String, Payment> byIdempotencyKey = new ConcurrentHashMap<>();

    @Override
    public synchronized Payment save(Payment payment) {
        Payment existing = byIdempotencyKey.putIfAbsent(payment.idempotencyKey(), payment);
        if (existing != null && !existing.id().equals(payment.id())) {
            throw new IllegalStateException("Idempotency key is already in use");
        }
        payments.put(payment.id(), payment);
        return payment;
    }

    @Override
    public Optional<Payment> findById(PaymentId id) {
        return Optional.ofNullable(payments.get(id));
    }

    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(byIdempotencyKey.get(idempotencyKey));
    }
}
