package com.rupeek.maidbooking.payment.domain;

import com.rupeek.maidbooking.maid.domain.Price;

import java.util.Objects;
import java.util.UUID;

public final class Payment {
    private final PaymentId id;
    private final UUID bookingId;
    private final Integer occurrenceIndex;
    private final Price amountSnapshot;
    private final PaymentMethodType method;
    private final String idempotencyKey;
    private PaymentStatus status;
    private String transactionReference;

    private Payment(UUID bookingId, Integer occurrenceIndex, Price amountSnapshot,
                    PaymentMethodType method, String idempotencyKey) {
        this.id = PaymentId.generate();
        this.bookingId = Objects.requireNonNull(bookingId);
        if (occurrenceIndex != null && occurrenceIndex < 0) {
            throw new IllegalArgumentException("Occurrence index must not be negative");
        }
        this.occurrenceIndex = occurrenceIndex;
        this.amountSnapshot = Objects.requireNonNull(amountSnapshot);
        this.method = Objects.requireNonNull(method);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("Idempotency key must not be blank");
        }
        this.idempotencyKey = idempotencyKey;
        this.status = PaymentStatus.INITIATED;
    }

    public static Payment initiate(UUID bookingId, Price amount, PaymentMethodType method, String idempotencyKey) {
        return initiate(bookingId, null, amount, method, idempotencyKey);
    }

    public static Payment initiate(UUID bookingId, Integer occurrenceIndex, Price amount,
                                  PaymentMethodType method, String idempotencyKey) {
        return new Payment(bookingId, occurrenceIndex, amount, method, idempotencyKey);
    }

    public void succeed(String transactionReference) {
        if (status != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Payment cannot be completed from status " + status);
        }
        this.status = PaymentStatus.SUCCESS;
        this.transactionReference = Objects.requireNonNull(transactionReference);
    }

    public void fail() {
        if (status != PaymentStatus.INITIATED) {
            throw new IllegalStateException("Payment cannot fail from status " + status);
        }
        this.status = PaymentStatus.FAILED;
    }

    public void refund() {
        if (status != PaymentStatus.SUCCESS) {
            throw new IllegalStateException("Only successful payments can be refunded");
        }
        status = PaymentStatus.REFUNDED;
    }

    public PaymentId id() { return id; }
    public UUID paymentId() { return id.value(); }
    public UUID bookingId() { return bookingId; }
    public Integer occurrenceIndex() { return occurrenceIndex; }
    public Price amountSnapshot() { return amountSnapshot; }
    public PaymentMethodType method() { return method; }
    public String idempotencyKey() { return idempotencyKey; }
    public PaymentStatus status() { return status; }
    public String transactionReference() { return transactionReference; }
}
