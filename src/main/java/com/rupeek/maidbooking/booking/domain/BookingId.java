package com.rupeek.maidbooking.booking.domain;

import java.util.Objects;
import java.util.UUID;

public record BookingId(UUID value) {
    public BookingId {
        Objects.requireNonNull(value, "Booking id is required");
    }

    public static BookingId generate() {
        return new BookingId(UUID.randomUUID());
    }
}
