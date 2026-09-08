package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.maid.domain.Maid;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@Component
public class InstantBookingStrategy implements BookingStrategy {
    @Override
    public BookingType supports() { return BookingType.INSTANT; }

    @Override
    public Booking createBooking(Maid maid, CreateBookingCommand command) {
        OffsetDateTime start = OffsetDateTime.now();
        TimeSlot slot = new TimeSlot(start, start.plus(Duration.ofHours(1)));
        ensureAvailable(maid, slot);
        return Booking.create(command.customerId(), maid.id(), supports(), command.services(),
                maid.calculatePrice(command.services()), List.of(slot));
    }

    static void ensureAvailable(Maid maid, TimeSlot slot) {
        if (!maid.isAvailable(slot.start().getDayOfWeek(), slot.start().toLocalTime(), slot.end().toLocalTime())) {
            throw new IllegalArgumentException("Maid is not available for the requested slot");
        }
    }
}
