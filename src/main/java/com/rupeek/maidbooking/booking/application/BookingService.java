package com.rupeek.maidbooking.booking.application;

import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.maid.application.MaidService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BookingService {
    private final MaidService maidService;
    private final BookingRepository bookingRepository;
    private final BookingStrategyRegistry strategyRegistry;

    public BookingService(MaidService maidService, BookingRepository bookingRepository,
                          BookingStrategyRegistry strategyRegistry) {
        this.maidService = maidService;
        this.bookingRepository = bookingRepository;
        this.strategyRegistry = strategyRegistry;
    }

    public Booking create(CreateBookingCommand command, UUID maidId) {
        var maid = maidService.get(maidId);
        BookingStrategy strategy = strategyRegistry.get(command.type());
        return bookingRepository.reserveAndSave(strategy.createBooking(maid, command));
    }

    public Booking get(UUID bookingId) {
        return bookingRepository.findById(new BookingId(bookingId))
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    public Booking cancel(UUID bookingId) {
        Booking booking = get(bookingId);
        booking.cancel();
        return booking;
    }

    public static class BookingNotFoundException extends RuntimeException {
        public BookingNotFoundException(UUID bookingId) {
            super("Booking not found: " + bookingId);
        }
    }
}
