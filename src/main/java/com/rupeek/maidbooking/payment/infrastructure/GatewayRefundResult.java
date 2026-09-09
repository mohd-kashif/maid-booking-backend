package com.rupeek.maidbooking.payment.infrastructure;

public record GatewayRefundResult(boolean successful, String refundReference, String failureReason) {
    public static GatewayRefundResult success(String reference) {
        return new GatewayRefundResult(true, reference, null);
    }
}
