package com.rupeek.maidbooking.payment.api;

import com.rupeek.maidbooking.payment.application.MakePaymentCommand;
import com.rupeek.maidbooking.payment.application.PaymentService;
import com.rupeek.maidbooking.payment.domain.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(@Valid @RequestBody MakePaymentRequest request) {
        Payment payment = service.makePayment(new MakePaymentCommand(request.bookingId(), request.method(),
                request.idempotencyKey(), request.paymentDetails(), request.occurrenceIndex()));
        return PaymentResponse.from(payment);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@PathVariable UUID paymentId) {
        return PaymentResponse.from(service.get(paymentId));
    }

    public record MakePaymentRequest(
            @NotNull UUID bookingId,
            @NotNull PaymentMethodType method,
            @NotBlank String idempotencyKey,
            String paymentDetails,
            Integer occurrenceIndex) {}

    public record PaymentResponse(UUID id, UUID bookingId, Integer occurrenceIndex,
                                  BigDecimal amount, String currency,
                                 PaymentMethodType method, PaymentStatus status,
                                 String transactionReference) {
        static PaymentResponse from(Payment payment) {
            return new PaymentResponse(payment.paymentId(), payment.bookingId(), payment.occurrenceIndex(),
                    payment.amountSnapshot().amount(), payment.amountSnapshot().currency(),
                    payment.method(), payment.status(), payment.transactionReference());
        }
    }
}
