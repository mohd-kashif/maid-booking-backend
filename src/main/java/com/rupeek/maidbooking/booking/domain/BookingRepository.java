package com.rupeek.maidbooking.booking.domain;

import java.util.Optional;

public interface BookingRepository {
    Booking reserveAndSave(Booking booking);
    Optional<Booking> findById(BookingId id);
}
