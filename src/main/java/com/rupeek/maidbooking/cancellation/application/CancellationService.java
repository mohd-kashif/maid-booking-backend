package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.cancellation.domain.*;
import com.rupeek.maidbooking.cancellation.infrastructure.InMemoryCancellationRepository;
import com.rupeek.maidbooking.payment.application.PaymentService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class CancellationService {
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final CancellationPolicyRegistry policyRegistry;
    private final InMemoryCancellationRepository repository;

    public CancellationService(BookingService bookingService, PaymentService paymentService,
                               CancellationPolicyRegistry policyRegistry, InMemoryCancellationRepository repository) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.policyRegistry = policyRegistry;
        this.repository = repository;
    }

    public synchronized Cancellation cancel(CancelBookingCommand command) {
        Booking booking = bookingService.get(command.bookingId());
        CancellationPolicy policy = policyRegistry.get(command.policyType());
        if (command.scope() == CancellationScope.SINGLE_OCCURRENCE) {
            booking.cancelOccurrence(requiredOccurrence(command));
            return repository.save(cancelOccurrencePayment(booking, command, policy));
        }

        if (booking.type() == com.rupeek.maidbooking.booking.domain.BookingType.RECURRING) {
            for (int index = 0; index < booking.slots().size(); index++) {
                if (booking.isOccurrenceActive(index)) {
                    refundIfAllowed(booking, index, policy);
                }
            }
        } else {
            refundIfAllowed(booking, null, policy);
        }
        booking.cancel();
        RefundDecision decision = policy.evaluate(booking, null, OffsetDateTime.now());
        return repository.save(Cancellation.completed(booking.bookingId(), command.scope(), null,
                command.reason(), decision));
    }

    private Cancellation cancelOccurrencePayment(Booking booking, CancelBookingCommand command,
                                                CancellationPolicy policy) {
        int index = requiredOccurrence(command);
        RefundDecision decision = policy.evaluate(booking, index, OffsetDateTime.now());
        if (decision.refundable()) {
            paymentService.refundIfPresent(booking.bookingId(), index);
        }
        return Cancellation.completed(booking.bookingId(), command.scope(), index, command.reason(), decision);
    }

    private void refundIfAllowed(Booking booking, Integer occurrenceIndex, CancellationPolicy policy) {
        RefundDecision decision = policy.evaluate(booking, occurrenceIndex, OffsetDateTime.now());
        if (decision.refundable()) {
            paymentService.refundIfPresent(booking.bookingId(), occurrenceIndex);
        }
    }

    private static int requiredOccurrence(CancelBookingCommand command) {
        if (command.occurrenceIndex() == null) {
            throw new IllegalArgumentException("Occurrence index is required");
        }
        return command.occurrenceIndex();
    }
}
