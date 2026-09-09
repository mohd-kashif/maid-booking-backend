package com.rupeek.maidbooking.payment.application;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.payment.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {
    private final BookingService bookingService;
    private final PaymentRepository paymentRepository;
    private final PaymentMethodRegistry methodRegistry;

    public PaymentService(BookingService bookingService, PaymentRepository paymentRepository,
                          PaymentMethodRegistry methodRegistry) {
        this.bookingService = bookingService;
        this.paymentRepository = paymentRepository;
        this.methodRegistry = methodRegistry;
    }

    public synchronized Payment makePayment(MakePaymentCommand command) {
        Payment existing = paymentRepository.findByIdempotencyKey(command.idempotencyKey()).orElse(null);
        if (existing != null) {
            if (!existing.bookingId().equals(command.bookingId())) {
                throw new IllegalStateException("Idempotency key belongs to another booking");
            }
            return existing;
        }

        Booking booking = bookingService.get(command.bookingId());
        if (!booking.isActive()) {
            throw new IllegalStateException("Cannot pay for a cancelled booking");
        }
        if (command.occurrenceIndex() != null
                && (booking.type() != com.rupeek.maidbooking.booking.domain.BookingType.RECURRING
                || command.occurrenceIndex() >= booking.slots().size())) {
            throw new IllegalArgumentException("Invalid recurring booking occurrence");
        }
        Payment payment = Payment.initiate(command.bookingId(), command.occurrenceIndex(), booking.priceSnapshot(),
                command.method(), command.idempotencyKey());
        methodRegistry.get(command.method()).process(payment, command.paymentDetails());
        return paymentRepository.save(payment);
    }

    public Payment get(UUID paymentId) {
        return paymentRepository.findById(new PaymentId(paymentId))
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    public static class PaymentNotFoundException extends RuntimeException {
        public PaymentNotFoundException(UUID paymentId) {
            super("Payment not found: " + paymentId);
        }
    }
}
