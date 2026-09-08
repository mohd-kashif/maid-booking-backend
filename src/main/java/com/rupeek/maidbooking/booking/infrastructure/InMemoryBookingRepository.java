package com.rupeek.maidbooking.booking.infrastructure;

import com.rupeek.maidbooking.booking.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<BookingId, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public synchronized Booking reserveAndSave(Booking booking) {
        boolean overlaps = bookings.values().stream()
                .filter(existing -> existing.maidId().equals(booking.maidId()) && existing.isActive())
                .anyMatch(existing -> existing.slots().stream()
                        .anyMatch(existingSlot -> booking.slots().stream().anyMatch(existingSlot::overlaps)));
        if (overlaps) {
            throw new IllegalStateException("Maid is already booked for one or more requested slots");
        }
        bookings.put(booking.id(), booking);
        return booking;
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        return Optional.ofNullable(bookings.get(id));
    }
}
