package com.rupeek.maidbooking.payment;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.booking.domain.BookingType;
import com.rupeek.maidbooking.booking.domain.TimeSlot;
import com.rupeek.maidbooking.maid.domain.*;
import com.rupeek.maidbooking.payment.application.*;
import com.rupeek.maidbooking.payment.domain.*;
import com.rupeek.maidbooking.payment.infrastructure.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentTest {
    @Test
    void processesPaymentAndKeepsBookingPriceSnapshot() {
        Booking booking = booking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(booking.bookingId())).thenReturn(booking);
        PaymentService paymentService = service(bookingService);

        Payment payment = paymentService.makePayment(new MakePaymentCommand(booking.bookingId(),
                PaymentMethodType.UPI, "key-1", "customer@upi", null));

        assertEquals(PaymentStatus.SUCCESS, payment.status());
        assertEquals(BigDecimal.valueOf(1000), payment.amountSnapshot().amount());
        assertNotNull(payment.transactionReference());
    }

    @Test
    void returnsSamePaymentForIdempotentRetry() {
        Booking booking = booking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(booking.bookingId())).thenReturn(booking);
        PaymentService paymentService = service(bookingService);
        var command = new MakePaymentCommand(booking.bookingId(), PaymentMethodType.CARD, "key-1", "card", null);

        Payment first = paymentService.makePayment(command);
        Payment retry = paymentService.makePayment(command);

        assertEquals(first.paymentId(), retry.paymentId());
        verify(bookingService, times(1)).get(booking.bookingId());
    }

    @Test
    void recordsGatewayFailure() {
        Booking booking = booking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(booking.bookingId())).thenReturn(booking);
        Payment payment = service(bookingService).makePayment(new MakePaymentCommand(booking.bookingId(),
                PaymentMethodType.WALLET, "key-fail", "fail", null));

        assertEquals(PaymentStatus.FAILED, payment.status());
    }

    @Test
    void rejectsIdempotencyKeyReuseForDifferentBooking() {
        Booking firstBooking = booking();
        Booking secondBooking = booking();
        BookingService bookingService = mock(BookingService.class);
        when(bookingService.get(firstBooking.bookingId())).thenReturn(firstBooking);
        when(bookingService.get(secondBooking.bookingId())).thenReturn(secondBooking);
        PaymentService paymentService = service(bookingService);
        String key = "shared-key";

        paymentService.makePayment(new MakePaymentCommand(firstBooking.bookingId(),
                PaymentMethodType.CARD, key, "card", null));

        assertThrows(IllegalStateException.class, () -> paymentService.makePayment(new MakePaymentCommand(
                secondBooking.bookingId(), PaymentMethodType.CARD, key, "card", null)));
    }

    private static PaymentService service(BookingService bookingService) {
        var gateway = new MockPaymentProvider();
        var registry = new PaymentMethodRegistry(List.of(
                new CardPaymentStrategy(gateway), new UpiPaymentStrategy(gateway), new WalletPaymentStrategy(gateway)));
        return new PaymentService(bookingService, new InMemoryPaymentRepository(), registry,
                gateway);
    }

    private static Booking booking() {
        return Booking.create("customer-1", new MaidId(UUID.randomUUID()), BookingType.SCHEDULED,
                Set.of(ServiceType.CLEANING), new Price(BigDecimal.valueOf(1000), "INR"),
                List.of(new TimeSlot(OffsetDateTime.now(ZoneOffset.UTC),
                        OffsetDateTime.now(ZoneOffset.UTC).plusHours(1))));
    }
}
