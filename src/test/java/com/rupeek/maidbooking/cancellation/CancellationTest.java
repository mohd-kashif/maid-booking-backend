package com.rupeek.maidbooking.cancellation;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.domain.*;
import com.rupeek.maidbooking.cancellation.application.*;
import com.rupeek.maidbooking.cancellation.domain.*;
import com.rupeek.maidbooking.cancellation.infrastructure.InMemoryCancellationRepository;
import com.rupeek.maidbooking.maid.domain.*;
import com.rupeek.maidbooking.payment.application.PaymentService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CancellationTest {
    @Test
    void cancelsSingleRecurringOccurrenceAndLeavesOtherSlotsActive() {
        Booking booking = recurringBooking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(booking.bookingId())).thenReturn(booking);
        CancellationService service = new CancellationService(bookingService, mock(PaymentService.class),
                new CancellationPolicyRegistry(List.of(new SimpleCancellationPolicy())),
                new InMemoryCancellationRepository());

        Cancellation cancellation = service.cancel(new CancelBookingCommand(booking.bookingId(),
                CancellationScope.SINGLE_OCCURRENCE, 0, CancellationReason.CUSTOMER_REQUEST,
                CancellationPolicyType.STANDARD));

        assertEquals(CancellationStatus.COMPLETED, cancellation.status());
        assertFalse(booking.isOccurrenceActive(0));
        assertTrue(booking.isOccurrenceActive(1));
        assertEquals(1, booking.activeSlots().size());
    }

    @Test
    void cancelsEntireBooking() {
        Booking booking = recurringBooking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(booking.bookingId())).thenReturn(booking);
        CancellationService service = new CancellationService(bookingService, mock(PaymentService.class),
                new CancellationPolicyRegistry(List.of(new SimpleCancellationPolicy())),
                new InMemoryCancellationRepository());

        service.cancel(new CancelBookingCommand(booking.bookingId(), CancellationScope.ENTIRE_BOOKING,
                null, CancellationReason.CUSTOMER_REQUEST, CancellationPolicyType.STANDARD));

        assertEquals(BookingStatus.CANCELLED, booking.status());
        assertTrue(booking.activeSlots().isEmpty());
    }

    @Test
    void rejectsDuplicatePolicyTypes() {
        CancellationPolicy duplicate = new CancellationPolicy() {
            @Override
            public CancellationPolicyType supports() { return CancellationPolicyType.STANDARD; }

            @Override
            public RefundDecision evaluate(Booking booking, Integer occurrenceIndex,
                                           java.time.OffsetDateTime now) {
                return new RefundDecision(false, null, "duplicate");
            }
        };

        assertThrows(IllegalStateException.class, () -> new CancellationPolicyRegistry(
                List.of(new SimpleCancellationPolicy(), duplicate)));
    }

    private static Booking recurringBooking() {
        OffsetDateTime start = OffsetDateTime.now(ZoneOffset.UTC).plusDays(3);
        return Booking.create("customer-1", new MaidId(UUID.randomUUID()), BookingType.RECURRING,
                Set.of(ServiceType.CLEANING), new Price(BigDecimal.valueOf(500), "INR"),
                List.of(new TimeSlot(start, start.plusHours(1)),
                        new TimeSlot(start.plusWeeks(1), start.plusWeeks(1).plusHours(1))));
    }
}
