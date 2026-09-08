package com.rupeek.maidbooking.booking.api;

import com.rupeek.maidbooking.booking.application.BookingService;
import com.rupeek.maidbooking.booking.application.CreateBookingCommand;
import com.rupeek.maidbooking.booking.domain.Booking;
import com.rupeek.maidbooking.booking.domain.BookingStatus;
import com.rupeek.maidbooking.booking.domain.BookingType;
import com.rupeek.maidbooking.booking.domain.TimeSlot;
import com.rupeek.maidbooking.maid.domain.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest request) {
        Booking booking = service.create(new CreateBookingCommand(request.customerId(), request.type(),
                request.services(), request.start(), request.end(), request.recurrenceEndDate()), request.maidId());
        return BookingResponse.from(booking);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse get(@PathVariable UUID bookingId) {
        return BookingResponse.from(service.get(bookingId));
    }

    @PostMapping("/{bookingId}/cancel")
    public BookingResponse cancel(@PathVariable UUID bookingId) {
        return BookingResponse.from(service.cancel(bookingId));
    }

    public record CreateBookingRequest(
            @NotBlank String customerId,
            @NotNull UUID maidId,
            @NotNull BookingType type,
            @NotEmpty Set<ServiceType> services,
            OffsetDateTime start,
            OffsetDateTime end,
            LocalDate recurrenceEndDate) {}

    public record BookingResponse(UUID id, String customerId, UUID maidId, BookingType type,
                                  Set<ServiceType> services, BigDecimal price, String currency,
                                  BookingStatus status, List<TimeSlot> slots) {
        static BookingResponse from(Booking booking) {
            return new BookingResponse(booking.bookingId(), booking.customerId(), booking.maidId().value(),
                    booking.type(), booking.services(), booking.priceSnapshot().amount(),
                    booking.priceSnapshot().currency(), booking.status(), booking.slots());
        }
    }
}
