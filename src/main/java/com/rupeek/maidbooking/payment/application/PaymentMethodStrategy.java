package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.payment.domain.Payment;
import com.rupeek.maidbooking.payment.domain.PaymentMethodType;

public interface PaymentMethodStrategy {
    PaymentMethodType supports();
    void process(Payment payment, String paymentDetails);
}
