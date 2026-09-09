package com.rupeek.maidbooking.discovery;

import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.booking.infrastructure.InMemoryBookingRepository;
import com.rupeek.maidbooking.discovery.application.*;
import com.rupeek.maidbooking.discovery.domain.*;
import com.rupeek.maidbooking.maid.domain.*;
import com.rupeek.maidbooking.maid.infrastructure.InMemoryMaidRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DiscoveryTest {
    @Test
    void findsMaidMatchingServicesAndPrice() {
        var maidRepository = new InMemoryMaidRepository();
        Maid maid = maid("Indiranagar", 4.5, Gender.FEMALE);
        maidRepository.save(maid);
        var service = service(maidRepository, new InMemoryBookingRepository());

        var results = service.search(new SearchMaidsQuery("indiranagar", Set.of(ServiceType.CLEANING),
                null, null, BigDecimal.valueOf(500), 4.0, Gender.FEMALE));

        assertEquals(List.of(maid), results);
    }

    @Test
    void excludesMaidWithOverlappingBooking() {
        var maidRepository = new InMemoryMaidRepository();
        Maid maid = maid("Indiranagar", 4.5, Gender.FEMALE);
        maidRepository.save(maid);
        var bookingRepository = new InMemoryBookingRepository();
        OffsetDateTime start = nextMondayAt(10);
        Booking booking = Booking.create("customer-1", maid.id(), BookingType.SCHEDULED,
                Set.of(ServiceType.CLEANING), new Price(BigDecimal.valueOf(400), "INR"),
                List.of(new TimeSlot(start, start.plusHours(1))));
        bookingRepository.reserveAndSave(booking);

        assertTrue(service(maidRepository, bookingRepository).search(new SearchMaidsQuery(null,
                Set.of(ServiceType.CLEANING), start, start.plusHours(1), null, null, null)).isEmpty());
    }

    @Test
    void rejectsIncompleteTimeRange() {
        assertThrows(IllegalArgumentException.class, () -> service(new InMemoryMaidRepository(),
                new InMemoryBookingRepository()).search(new SearchMaidsQuery(null, null,
                OffsetDateTime.now(), null, null, null, null)));
    }

    private static MaidDiscoveryService service(InMemoryMaidRepository maids, InMemoryBookingRepository bookings) {
        return new MaidDiscoveryService(maids, bookings, List.of(new LocalityFilter(), new ServiceFilter(),
                new PriceFilter(), new RatingFilter(), new GenderFilter()));
    }

    private static Maid maid(String locality, double rating, Gender gender) {
        return Maid.register("Asha", locality, List.of(
                        new ServiceOffering(ServiceType.CLEANING, new Price(BigDecimal.valueOf(400), "INR"))),
                List.of(new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0))),
                rating, gender);
    }

    private static OffsetDateTime nextMondayAt(int hour) {
        return LocalDate.now().plusWeeks(1).with(DayOfWeek.MONDAY)
                .atTime(hour, 0).atOffset(ZoneOffset.UTC);
    }
}
