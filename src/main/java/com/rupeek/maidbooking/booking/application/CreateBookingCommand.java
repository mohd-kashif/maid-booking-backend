package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.BookingType;
import com.rupeek.maidbooking.maid.domain.ServiceType;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.Set;

public record CreateBookingCommand(
        String customerId,
        BookingType type,
        Set<ServiceType> services,
        OffsetDateTime start,
        OffsetDateTime end,
        LocalDate recurrenceEndDate) {
}
