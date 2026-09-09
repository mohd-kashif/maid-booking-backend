package com.rupeek.maidbooking.cancellation.api;

import com.rupeek.maidbooking.cancellation.application.CancelBookingCommand;
import com.rupeek.maidbooking.cancellation.application.CancellationService;
import com.rupeek.maidbooking.cancellation.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings/{bookingId}/cancellations")
public class CancellationController {
    private final CancellationService service;

    public CancellationController(CancellationService service) {
        this.service = service;
    }

    @PostMapping
    public CancellationResponse cancel(@PathVariable UUID bookingId,
                                       @Valid @RequestBody CancelRequest request) {
        Cancellation cancellation = service.cancel(new CancelBookingCommand(bookingId, request.scope(),
                request.occurrenceIndex(), request.reason(), request.policyType() == null
                ? CancellationPolicyType.STANDARD : request.policyType()));
        RefundDecision decision = cancellation.refundDecision();
        return new CancellationResponse(cancellation.id(), cancellation.bookingId(), cancellation.scope(),
                cancellation.occurrenceIndex(), cancellation.status(), decision.refundable(),
                decision.amount() == null ? null : decision.amount().amount(),
                decision.amount() == null ? null : decision.amount().currency(), decision.reason(),
                cancellation.createdAt());
    }

    public record CancelRequest(@NotNull CancellationScope scope, Integer occurrenceIndex,
                                @NotNull CancellationReason reason,
                                CancellationPolicyType policyType) {}

    public record CancellationResponse(UUID id, UUID bookingId, CancellationScope scope,
                                       Integer occurrenceIndex, CancellationStatus status,
                                       boolean refundable, BigDecimal refundAmount, String currency,
                                       String refundReason, OffsetDateTime createdAt) {}
}
