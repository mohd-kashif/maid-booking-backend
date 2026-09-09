package com.rupeek.maidbooking.payment.infrastructure;

import com.rupeek.maidbooking.payment.domain.Payment;
import com.rupeek.maidbooking.maid.domain.Price;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Mock provider used for both charging and refunding the original transaction. */
@Component
public class MockPaymentProvider implements PaymentGateway, RefundGateway {
    @Override
    public GatewayPaymentResult charge(Payment payment, String paymentDetails) {
        if (paymentDetails != null && paymentDetails.equalsIgnoreCase("fail")) {
            return GatewayPaymentResult.failure("Mock gateway rejected the payment");
        }
        return GatewayPaymentResult.success("mock-txn-" + UUID.randomUUID());
    }

    @Override
    public GatewayRefundResult refund(String transactionReference, Price amount) {
        return GatewayRefundResult.success("mock-refund-" + UUID.randomUUID());
    }
}
