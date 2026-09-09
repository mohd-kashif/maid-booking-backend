package com.rupeek.maidbooking.maid.api;

import com.rupeek.maidbooking.maid.application.MaidService;
import com.rupeek.maidbooking.maid.domain.AvailabilityWindow;
import com.rupeek.maidbooking.maid.domain.Maid;
import com.rupeek.maidbooking.maid.domain.ServiceOffering;
import com.rupeek.maidbooking.maid.domain.Price;
import com.rupeek.maidbooking.maid.domain.ServiceType;
import com.rupeek.maidbooking.maid.domain.Gender;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
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
                request.services().stream()
                        .map(offering -> new ServiceOffering(offering.type(),
                                new Price(offering.price(), offering.currency())))
                        .toList(),
                request.availability().stream().map(MaidController::toDomain).toList(),
                request.rating() == null ? 0 : request.rating(), request.gender());
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
            @NotEmpty List<@Valid ServiceOfferingRequest> services,
            @NotEmpty List<@Valid AvailabilityRequest> availability,
            @DecimalMin("0.0") @DecimalMax("5.0") Double rating,
            Gender gender) {}

    public record ServiceOfferingRequest(
            @NotNull ServiceType type,
            @NotNull @Positive BigDecimal price,
            @NotBlank String currency) {}

    public record AvailabilityRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime) {}

    public record AvailabilityResponse(boolean available) {}

    public record MaidResponse(UUID id, String name, String locality, List<ServiceOfferingResponse> services,
                               List<AvailabilityRequest> availability, double rating, Gender gender) {
        static MaidResponse from(Maid maid) {
            return new MaidResponse(maid.id().value(), maid.name(), maid.locality(),
                    maid.serviceOfferings().stream()
                            .map(offering -> new ServiceOfferingResponse(offering.serviceType(),
                                    offering.price().amount(), offering.price().currency()))
                            .toList(), maid.availabilityWindows().stream()
                            .map(window -> new AvailabilityRequest(window.dayOfWeek(), window.startTime(), window.endTime()))
                            .toList(), maid.rating(), maid.gender());
        }
    }

    public record ServiceOfferingResponse(ServiceType type, BigDecimal price, String currency) {}
}
