package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.cancellation.domain.RefundDecision;
import com.rupeek.maidbooking.cancellation.domain.CancellationPolicyType;

import java.time.OffsetDateTime;

public interface CancellationPolicy {
    CancellationPolicyType supports();
    RefundDecision evaluate(Booking booking, Integer occurrenceIndex, OffsetDateTime now);
}
