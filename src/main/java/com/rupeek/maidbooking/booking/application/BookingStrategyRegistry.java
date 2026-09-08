package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.BookingType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class BookingStrategyRegistry {
    private final Map<BookingType, BookingStrategy> strategies;

    public BookingStrategyRegistry(List<BookingStrategy> strategies) {
        EnumMap<BookingType, BookingStrategy> registered = new EnumMap<>(BookingType.class);
        strategies.forEach(strategy -> {
            if (registered.put(strategy.supports(), strategy) != null) {
                throw new IllegalStateException("Duplicate booking strategy: " + strategy.supports());
            }
        });
        this.strategies = Map.copyOf(registered);
    }

    public BookingStrategy get(BookingType type) {
        BookingStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported booking type: " + type);
        }
        return strategy;
    }
}
