package com.rupeek.maidbooking.payment.infrastructure;

import com.rupeek.maidbooking.payment.domain.Payment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public GatewayPaymentResult charge(Payment payment, String paymentDetails) {
        if (paymentDetails != null && paymentDetails.equalsIgnoreCase("fail")) {
            return GatewayPaymentResult.failure("Mock gateway rejected the payment");
        }
        return GatewayPaymentResult.success("mock-txn-" + UUID.randomUUID());
    }
}
