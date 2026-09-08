package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.maid.domain.Maid;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class ScheduledBookingStrategy implements BookingStrategy {
    @Override
    public BookingType supports() { return BookingType.SCHEDULED; }

    @Override
    public Booking createBooking(Maid maid, CreateBookingCommand command) {
        if (command.start() == null || command.end() == null) {
            throw new IllegalArgumentException("Scheduled booking requires start and end times");
        }
        if (command.start().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Scheduled booking must be in the future");
        }
        TimeSlot slot = new TimeSlot(command.start(), command.end());
        InstantBookingStrategy.ensureAvailable(maid, slot);
        return Booking.create(command.customerId(), maid.id(), supports(), command.services(),
                maid.calculatePrice(command.services()), List.of(slot));
    }
}
