package com.rupeek.maidbooking.payment.infrastructure;

public record GatewayPaymentResult(boolean successful, String transactionReference, String failureReason) {
    public static GatewayPaymentResult success(String reference) {
        return new GatewayPaymentResult(true, reference, null);
    }

    public static GatewayPaymentResult failure(String reason) {
        return new GatewayPaymentResult(false, null, reason);
    }
}
