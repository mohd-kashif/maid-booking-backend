package com.rupeek.maidbooking.maid.domain;

import java.math.BigDecimal;
import java.util.Objects;

public record Price(BigDecimal amount, String currency) {
    public Price {
        Objects.requireNonNull(amount, "Price amount is required");
        Objects.requireNonNull(currency, "Currency is required");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
    }
}
