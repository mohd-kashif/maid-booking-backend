package com.rupeek.maidbooking.discovery.api;

import com.rupeek.maidbooking.discovery.application.MaidDiscoveryService;
import com.rupeek.maidbooking.discovery.application.SearchMaidsQuery;
import com.rupeek.maidbooking.maid.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/maids")
public class MaidDiscoveryController {
    private final MaidDiscoveryService service;

    public MaidDiscoveryController(MaidDiscoveryService service) {
        this.service = service;
    }

    @GetMapping
    public List<MaidSearchResponse> search(
            @RequestParam(required = false) String locality,
            @RequestParam(required = false) Set<ServiceType> services,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime end,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minimumRating,
            @RequestParam(required = false) Gender gender) {
        return service.search(new SearchMaidsQuery(locality, services, start, end, maxPrice, minimumRating, gender))
                .stream().map(MaidSearchResponse::from).toList();
    }

    public record MaidSearchResponse(UUID id, String name, String locality, List<ServiceOfferingResponse> services,
                                     double rating, Gender gender) {
        static MaidSearchResponse from(Maid maid) {
            return new MaidSearchResponse(maid.id().value(), maid.name(), maid.locality(),
                    maid.serviceOfferings().stream()
                            .map(offering -> new ServiceOfferingResponse(offering.serviceType(),
                                    offering.price().amount(), offering.price().currency()))
                            .toList(), maid.rating(), maid.gender());
        }
    }

    public record ServiceOfferingResponse(ServiceType type, BigDecimal price, String currency) {}
}
