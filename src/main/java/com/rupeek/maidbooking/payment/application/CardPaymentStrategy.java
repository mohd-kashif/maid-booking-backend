package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.payment.domain.PaymentMethodType;
import com.rupeek.maidbooking.payment.infrastructure.PaymentGateway;
import org.springframework.stereotype.Component;

@Component
public class CardPaymentStrategy extends AbstractPaymentMethodStrategy {
    public CardPaymentStrategy(PaymentGateway gateway) { super(gateway); }
    @Override public PaymentMethodType supports() { return PaymentMethodType.CARD; }
}
