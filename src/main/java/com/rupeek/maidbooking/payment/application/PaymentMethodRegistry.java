package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.payment.domain.PaymentMethodType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentMethodRegistry {
    private final Map<PaymentMethodType, PaymentMethodStrategy> strategies;

    public PaymentMethodRegistry(List<PaymentMethodStrategy> strategies) {
        EnumMap<PaymentMethodType, PaymentMethodStrategy> registered = new EnumMap<>(PaymentMethodType.class);
        strategies.forEach(strategy -> {
            if (registered.put(strategy.supports(), strategy) != null) {
                throw new IllegalStateException("Duplicate payment method strategy: " + strategy.supports());
            }
        });
        this.strategies = Map.copyOf(registered);
    }

    public PaymentMethodStrategy get(PaymentMethodType method) {
        PaymentMethodStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + method);
        }
        return strategy;
    }
}
