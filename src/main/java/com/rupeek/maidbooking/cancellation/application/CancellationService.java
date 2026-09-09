package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.cancellation.domain.*;
import com.rupeek.maidbooking.cancellation.infrastructure.InMemoryCancellationRepository;
import com.rupeek.maidbooking.payment.application.PaymentService;
import org.springframework.stereotype.Service;

import com.rupeek.maidbooking.maid.domain.Price;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
        if (!booking.isActive()) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        CancellationPolicy policy = policyRegistry.get(command.policyType());
        if (command.scope() == CancellationScope.SINGLE_OCCURRENCE) {
            return repository.save(cancelOccurrencePayment(booking, command, policy));
        }

        List<RefundDecision> decisions = new ArrayList<>();
        if (booking.type() == com.rupeek.maidbooking.booking.domain.BookingType.RECURRING) {
            for (int index = 0; index < booking.slots().size(); index++) {
                if (booking.isOccurrenceActive(index)) {
                    decisions.add(refundIfAllowed(booking, index, policy));
                }
            }
        } else {
            decisions.add(refundIfAllowed(booking, null, policy));
        }
        booking.cancel();
        RefundDecision decision = aggregate(decisions);
        return repository.save(Cancellation.completed(booking.bookingId(), command.scope(), null,
                command.reason(), decision));
    }

    private Cancellation cancelOccurrencePayment(Booking booking, CancelBookingCommand command,
                                                CancellationPolicy policy) {
        int index = requiredOccurrence(command, booking);
        RefundDecision decision = policy.evaluate(booking, index, OffsetDateTime.now());
        if (decision.refundable()) {
            paymentService.refundIfPresent(booking.bookingId(), index);
        }
        booking.cancelOccurrence(index);
        return Cancellation.completed(booking.bookingId(), command.scope(), index, command.reason(), decision);
    }

    private RefundDecision refundIfAllowed(Booking booking, Integer occurrenceIndex, CancellationPolicy policy) {
        RefundDecision decision = policy.evaluate(booking, occurrenceIndex, OffsetDateTime.now());
        if (decision.refundable()) {
            paymentService.refundIfPresent(booking.bookingId(), occurrenceIndex);
        }
        return decision;
    }

    private static RefundDecision aggregate(List<RefundDecision> decisions) {
        BigDecimal total = BigDecimal.ZERO;
        String currency = null;
        boolean refundable = false;
        List<String> reasons = new ArrayList<>();
        for (RefundDecision decision : decisions) {
            reasons.add(decision.reason());
            if (decision.refundable() && decision.amount() != null) {
                refundable = true;
                total = total.add(decision.amount().amount());
                currency = decision.amount().currency();
            }
        }
        Price amount = refundable ? new Price(total, currency) : null;
        return new RefundDecision(refundable, amount, String.join("; ", reasons));
    }

    private static int requiredOccurrence(CancelBookingCommand command, Booking booking) {
        if (command.occurrenceIndex() == null) {
            throw new IllegalArgumentException("Occurrence index is required");
        }
        if (booking.type() != com.rupeek.maidbooking.booking.domain.BookingType.RECURRING
                || command.occurrenceIndex() < 0
                || command.occurrenceIndex() >= booking.slots().size()) {
            throw new IllegalArgumentException("Invalid booking occurrence");
        }
        return command.occurrenceIndex();
    }
}
