package com.rupeek.maidbooking.payment.infrastructure;

import com.rupeek.maidbooking.payment.domain.Payment;

public interface PaymentGateway {
    GatewayPaymentResult charge(Payment payment, String paymentDetails);
}
