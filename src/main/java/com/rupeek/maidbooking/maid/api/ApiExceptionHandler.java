package com.rupeek.maidbooking.maid.api;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.payment.application.PaymentService;
import com.rupeek.maidbooking.maid.application.MaidService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PaymentService.PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> paymentNotFound(PaymentService.PaymentNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(BookingService.BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> bookingNotFound(BookingService.BookingNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(MaidService.MaidNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Map<String, String> notFound(MaidService.MaidNotFoundException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Map<String, String> badRequest(IllegalArgumentException exception) {
        return Map.of("error", exception.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    Map<String, String> conflict(IllegalStateException exception) {
        return Map.of("error", exception.getMessage());
    }
}
