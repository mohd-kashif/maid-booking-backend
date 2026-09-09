package com.rupeek.maidbooking.maid.api;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.payment.application.PaymentService;
import com.rupeek.maidbooking.maid.application.MaidService;
import com.rupeek.maidbooking.shared.api.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(PaymentService.PaymentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse paymentNotFound(PaymentService.PaymentNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "PAYMENT_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(BookingService.BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse bookingNotFound(BookingService.BookingNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "BOOKING_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(MaidService.MaidNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ApiErrorResponse maidNotFound(MaidService.MaidNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "MAID_NOT_FOUND", exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage()));
        return ApiErrorResponse.of(400, "VALIDATION_ERROR", "Request validation failed", fields,
                request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse constraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse malformedRequest(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "Request body or parameter is invalid", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ApiErrorResponse badRequest(IllegalArgumentException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    ApiErrorResponse conflict(IllegalStateException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request);
    }

    private static ApiErrorResponse error(HttpStatus status, String code, String message,
                                          HttpServletRequest request) {
        return ApiErrorResponse.of(status.value(), code, message, Map.of(), request.getRequestURI());
    }
}
