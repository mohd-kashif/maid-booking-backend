package com.rupeek.maidbooking.maid.api;

import com.rupeek.maidbooking.maid.application.MaidService;
import com.rupeek.maidbooking.maid.domain.AvailabilityWindow;
import com.rupeek.maidbooking.maid.domain.Maid;
import com.rupeek.maidbooking.maid.domain.ServiceType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/maids")
public class MaidController {
    private final MaidService service;

    public MaidController(MaidService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaidResponse register(@Valid @RequestBody RegisterMaidRequest request) {
        Maid maid = service.register(request.name(), request.locality(),
                EnumSet.copyOf(request.services()),
                new com.rupeek.maidbooking.maid.domain.Price(request.price(), request.currency()),
                request.availability().stream().map(MaidController::toDomain).toList());
        return MaidResponse.from(maid);
    }

    @GetMapping("/{maidId}")
    public MaidResponse get(@PathVariable UUID maidId) {
        return MaidResponse.from(service.get(maidId));
    }

    @PostMapping("/{maidId}/availability")
    public MaidResponse addAvailability(@PathVariable UUID maidId,
                                        @Valid @RequestBody AvailabilityRequest request) {
        return MaidResponse.from(service.addAvailability(maidId, toDomain(request)));
    }

    @GetMapping("/{maidId}/availability")
    public AvailabilityResponse checkAvailability(@PathVariable UUID maidId,
                                                  @RequestParam DayOfWeek day,
                                                  @RequestParam LocalTime start,
                                                  @RequestParam LocalTime end) {
        return new AvailabilityResponse(service.isAvailable(maidId, day, start, end));
    }

    private static AvailabilityWindow toDomain(AvailabilityRequest request) {
        return new AvailabilityWindow(request.dayOfWeek(), request.startTime(), request.endTime());
    }

    public record RegisterMaidRequest(
            @NotBlank String name,
            @NotBlank String locality,
            @NotEmpty Set<ServiceType> services,
            @NotNull @Positive BigDecimal price,
            @NotBlank String currency,
            @NotEmpty List<@Valid AvailabilityRequest> availability) {}

    public record AvailabilityRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime) {}

    public record AvailabilityResponse(boolean available) {}

    public record MaidResponse(UUID id, String name, String locality, Set<ServiceType> services,
                               BigDecimal price, String currency, List<AvailabilityRequest> availability) {
        static MaidResponse from(Maid maid) {
            return new MaidResponse(maid.id().value(), maid.name(), maid.locality(), maid.services(),
                    maid.price().amount(), maid.price().currency(), maid.availabilityWindows().stream()
                            .map(window -> new AvailabilityRequest(window.dayOfWeek(), window.startTime(), window.endTime()))
                            .toList());
        }
    }
}
