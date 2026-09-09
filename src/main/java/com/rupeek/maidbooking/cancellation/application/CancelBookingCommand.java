package com.rupeek.maidbooking.cancellation.application;

import com.rupeek.maidbooking.cancellation.domain.CancellationReason;
import com.rupeek.maidbooking.cancellation.domain.CancellationScope;
import com.rupeek.maidbooking.cancellation.domain.CancellationPolicyType;

import java.util.UUID;

public record CancelBookingCommand(UUID bookingId, CancellationScope scope,
                                   Integer occurrenceIndex, CancellationReason reason,
                                   CancellationPolicyType policyType) {
}
