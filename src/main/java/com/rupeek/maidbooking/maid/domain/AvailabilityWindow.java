package com.rupeek.maidbooking.maid.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;

public record AvailabilityWindow(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    public AvailabilityWindow {
        Objects.requireNonNull(dayOfWeek, "Day of week is required");
        Objects.requireNonNull(startTime, "Start time is required");
        Objects.requireNonNull(endTime, "End time is required");
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Availability start time must be before end time");
        }
    }

    public boolean contains(LocalTime requestedStart, LocalTime requestedEnd) {
        return !requestedStart.isBefore(startTime) && !requestedEnd.isAfter(endTime);
    }

    public boolean overlaps(AvailabilityWindow other) {
        return dayOfWeek == other.dayOfWeek
                && startTime.isBefore(other.endTime)
                && other.startTime.isBefore(endTime);
    }
}
