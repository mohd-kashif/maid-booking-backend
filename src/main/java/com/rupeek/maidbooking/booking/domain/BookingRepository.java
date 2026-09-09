package com.rupeek.maidbooking.booking.domain;

import java.util.Optional;
import com.rupeek.maidbooking.maid.domain.MaidId;

public interface BookingRepository {
    Booking reserveAndSave(Booking booking);
    Optional<Booking> findById(BookingId id);
    boolean hasActiveOverlap(MaidId maidId, TimeSlot slot);
}
