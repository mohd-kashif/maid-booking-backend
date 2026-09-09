package com.rupeek.maidbooking.cancellation.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Cancellation(UUID id, UUID bookingId, CancellationScope scope,
                           Integer occurrenceIndex, CancellationReason reason,
                           CancellationStatus status, RefundDecision refundDecision,
                           OffsetDateTime createdAt) {
    public static Cancellation completed(UUID bookingId, CancellationScope scope, Integer occurrenceIndex,
                                         CancellationReason reason, RefundDecision refundDecision) {
        return new Cancellation(UUID.randomUUID(), bookingId, scope, occurrenceIndex, reason,
                CancellationStatus.COMPLETED, refundDecision, OffsetDateTime.now());
    }
}
