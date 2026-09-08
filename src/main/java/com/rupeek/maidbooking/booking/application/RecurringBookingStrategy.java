package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.maid.domain.Maid;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecurringBookingStrategy implements BookingStrategy {
    @Override
    public BookingType supports() { return BookingType.RECURRING; }

    @Override
    public Booking createBooking(Maid maid, CreateBookingCommand command) {
        if (command.start() == null || command.end() == null || command.recurrenceEndDate() == null) {
            throw new IllegalArgumentException("Recurring booking requires start, end, and recurrence end date");
        }
        if (command.recurrenceEndDate().isBefore(command.start().toLocalDate())) {
            throw new IllegalArgumentException("Recurrence end date must not be before the start date");
        }
        List<TimeSlot> slots = new ArrayList<>();
        OffsetDateTime start = command.start();
        OffsetDateTime end = command.end();
        while (!start.toLocalDate().isAfter(command.recurrenceEndDate())) {
            TimeSlot slot = new TimeSlot(start, end);
            InstantBookingStrategy.ensureAvailable(maid, slot);
            slots.add(slot);
            start = start.plusWeeks(1);
            end = end.plusWeeks(1);
        }
        return Booking.create(command.customerId(), maid.id(), supports(), command.services(),
                maid.calculatePrice(command.services()), slots);
    }
}
