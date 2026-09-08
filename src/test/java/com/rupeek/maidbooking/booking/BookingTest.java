package com.rupeek.maidbooking.booking;

import com.rupeek.maidbooking.booking.application.CreateBookingCommand;
import com.rupeek.maidbooking.booking.application.ScheduledBookingStrategy;
import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.booking.infrastructure.InMemoryBookingRepository;
import com.rupeek.maidbooking.maid.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {
    private final ScheduledBookingStrategy strategy = new ScheduledBookingStrategy();

    @Test
    void createsBookingWithPriceSnapshot() {
        Maid maid = maid();
        OffsetDateTime start = nextMondayAt(10);
        Booking booking = strategy.createBooking(maid, new CreateBookingCommand(
                "customer-1", BookingType.SCHEDULED, Set.of(ServiceType.CLEANING, ServiceType.COOKING),
                start, start.plusHours(2), null));

        assertEquals(BigDecimal.valueOf(1000), booking.priceSnapshot().amount());
        assertEquals(BookingStatus.CONFIRMED, booking.status());
    }

    @Test
    void rejectsOverlappingActiveBookings() {
        Maid maid = maid();
        OffsetDateTime start = nextMondayAt(10);
        var repository = new InMemoryBookingRepository();
        Booking first = strategy.createBooking(maid, command(start));
        Booking second = strategy.createBooking(maid, command(start.plusMinutes(30)));

        repository.reserveAndSave(first);
        assertThrows(IllegalStateException.class, () -> repository.reserveAndSave(second));
    }

    @Test
    void allowsSlotAfterCancellation() {
        Maid maid = maid();
        OffsetDateTime start = nextMondayAt(10);
        var repository = new InMemoryBookingRepository();
        Booking first = strategy.createBooking(maid, command(start));
        repository.reserveAndSave(first);
        first.cancel();

        assertDoesNotThrow(() -> repository.reserveAndSave(strategy.createBooking(maid, command(start))));
    }

    @Test
    void rejectsSecondCancellation() {
        Booking booking = strategy.createBooking(maid(), command(nextMondayAt(10)));
        booking.cancel();

        assertThrows(IllegalStateException.class, booking::cancel);
    }

    private static CreateBookingCommand command(OffsetDateTime start) {
        return new CreateBookingCommand("customer-1", BookingType.SCHEDULED,
                Set.of(ServiceType.CLEANING), start, start.plusHours(1), null);
    }

    private static Maid maid() {
        return Maid.register("Asha", "Indiranagar", List.of(
                        new ServiceOffering(ServiceType.CLEANING, new Price(BigDecimal.valueOf(400), "INR")),
                        new ServiceOffering(ServiceType.COOKING, new Price(BigDecimal.valueOf(600), "INR"))),
                List.of(new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))));
    }

    private static OffsetDateTime nextMondayAt(int hour) {
        LocalDate date = LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY);
        return date.atTime(hour, 0).atOffset(ZoneOffset.UTC);
    }
}
