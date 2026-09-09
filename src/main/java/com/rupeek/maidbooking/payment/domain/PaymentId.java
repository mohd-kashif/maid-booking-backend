package com.rupeek.maidbooking.payment.domain;

import java.util.UUID;

public record PaymentId(UUID value) {
    public static PaymentId generate() { return new PaymentId(UUID.randomUUID()); }
}
