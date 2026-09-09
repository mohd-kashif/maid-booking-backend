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
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    @Test
    void allowsOnlyOneConcurrentReservationForSameSlot() throws Exception {
        var repository = new InMemoryBookingRepository();
        OffsetDateTime start = nextMondayAt(10);
        int attempts = 8;
        var ready = new CountDownLatch(attempts);
        var startGate = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(attempts);
        Maid maid = maid();
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < attempts; i++) {
                results.add(executor.submit(() -> {
                    ready.countDown();
                    startGate.await();
                    try {
                        repository.reserveAndSave(strategy.createBooking(maid, command(start)));
                        return true;
                    } catch (IllegalStateException exception) {
                        return false;
                    }
                }));
            }
            ready.await();
            startGate.countDown();

            long successfulReservations = results.stream().filter(result -> {
                try { return result.get(); } catch (Exception exception) { throw new RuntimeException(exception); }
            }).count();
            assertEquals(1, successfulReservations);
        } finally {
            executor.shutdownNow();
        }
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
