package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.payment.domain.PaymentMethodType;

import java.util.UUID;

public record MakePaymentCommand(UUID bookingId, PaymentMethodType method,
                                 String idempotencyKey, String paymentDetails,
                                 Integer occurrenceIndex) {
}
