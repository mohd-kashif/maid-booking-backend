package com.rupeek.maidbooking.booking.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public record TimeSlot(OffsetDateTime start, OffsetDateTime end) {
    public TimeSlot {
        Objects.requireNonNull(start, "Start time is required");
        Objects.requireNonNull(end, "End time is required");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Slot start must be before slot end");
        }
    }

    public boolean overlaps(TimeSlot other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
