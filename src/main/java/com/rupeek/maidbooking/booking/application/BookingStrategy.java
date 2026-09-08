package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.booking.domain.BookingType;
import com.rupeek.maidbooking.maid.domain.Maid;

public interface BookingStrategy {
    BookingType supports();
    Booking createBooking(Maid maid, CreateBookingCommand command);
}
