package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.cancellation.domain.RefundDecision;
import com.rupeek.maidbooking.cancellation.domain.CancellationPolicyType;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class SimpleCancellationPolicy implements CancellationPolicy {
    @Override
    public CancellationPolicyType supports() { return CancellationPolicyType.STANDARD; }

    @Override
    public RefundDecision evaluate(Booking booking, Integer occurrenceIndex, OffsetDateTime now) {
        var slot = occurrenceIndex == null ? booking.slots().get(0) : booking.slots().get(occurrenceIndex);
        if (slot.start().isAfter(now.plusHours(24))) {
            return new RefundDecision(true, booking.priceSnapshot(), "Cancelled more than 24 hours in advance");
        }
        return new RefundDecision(false, null, "Cancellation is within 24 hours of the slot");
    }
}
