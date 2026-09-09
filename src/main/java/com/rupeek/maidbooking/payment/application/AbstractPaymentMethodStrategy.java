package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.payment.domain.Payment;
import com.rupeek.maidbooking.payment.infrastructure.GatewayPaymentResult;
import com.rupeek.maidbooking.payment.infrastructure.PaymentGateway;

abstract class AbstractPaymentMethodStrategy implements PaymentMethodStrategy {
    private final PaymentGateway gateway;

    protected AbstractPaymentMethodStrategy(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public void process(Payment payment, String paymentDetails) {
        GatewayPaymentResult result = gateway.charge(payment, paymentDetails);
        if (result.successful()) {
            payment.succeed(result.transactionReference());
        } else {
            payment.fail();
        }
    }
}
