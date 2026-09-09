package com.rupeek.maidbooking.booking.infrastructure;

import com.rupeek.maidbooking.booking.domain.*;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import com.rupeek.maidbooking.booking.domain.TimeSlot;
import com.rupeek.maidbooking.maid.domain.MaidId;

@Repository
public class InMemoryBookingRepository implements BookingRepository {
    private final Map<BookingId, Booking> bookings = new ConcurrentHashMap<>();

    @Override
    public synchronized Booking reserveAndSave(Booking booking) {
        boolean overlaps = bookings.values().stream()
                .filter(existing -> existing.maidId().equals(booking.maidId()) && existing.isActive())
                .anyMatch(existing -> existing.activeSlots().stream()
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

    @Override
    public boolean hasActiveOverlap(MaidId maidId, TimeSlot slot) {
        return bookings.values().stream()
                .filter(booking -> booking.maidId().equals(maidId) && booking.isActive())
                .flatMap(booking -> booking.activeSlots().stream())
                .anyMatch(slot::overlaps);
    }
}
