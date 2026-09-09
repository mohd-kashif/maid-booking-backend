package com.rupeek.maidbooking.maid.domain;

import java.util.ArrayList;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class Maid {
    private final MaidId id;
    private final String name;
    private final String locality;
    private final List<ServiceOffering> serviceOfferings;
    private final MaidStatus status;
    private final List<AvailabilityWindow> availabilityWindows;
    private final double rating;
    private final Gender gender;

    private Maid(MaidId id, String name, String locality, List<ServiceOffering> serviceOfferings,
                 MaidStatus status, List<AvailabilityWindow> availabilityWindows,
                 double rating, Gender gender) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "Name");
        this.locality = requireText(locality, "Locality");
        if (serviceOfferings == null || serviceOfferings.isEmpty()) {
            throw new IllegalArgumentException("At least one service offering is required");
        }
        this.serviceOfferings = List.copyOf(serviceOfferings);
        validateUniqueServices(this.serviceOfferings);
        validateSameCurrency(this.serviceOfferings);
        this.status = Objects.requireNonNull(status);
        this.availabilityWindows = List.copyOf(availabilityWindows);
        if (rating < 0 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        this.rating = rating;
        this.gender = gender;
        validateNoOverlappingWindows(this.availabilityWindows);
    }

    public static Maid register(String name, String locality, List<ServiceOffering> serviceOfferings,
                                List<AvailabilityWindow> availabilityWindows) {
        if (availabilityWindows == null || availabilityWindows.isEmpty()) {
            throw new IllegalArgumentException("At least one availability window is required");
        }
        return new Maid(MaidId.generate(), name, locality, serviceOfferings, MaidStatus.ACTIVE,
                availabilityWindows, 0, null);
    }

    public static Maid register(String name, String locality, List<ServiceOffering> serviceOfferings,
                                List<AvailabilityWindow> availabilityWindows, double rating, Gender gender) {
        if (availabilityWindows == null || availabilityWindows.isEmpty()) {
            throw new IllegalArgumentException("At least one availability window is required");
        }
        return new Maid(MaidId.generate(), name, locality, serviceOfferings, MaidStatus.ACTIVE,
                availabilityWindows, rating, gender);
    }

    public Maid addAvailability(AvailabilityWindow window) {
        Objects.requireNonNull(window, "Availability window is required");
        var updated = new ArrayList<>(availabilityWindows);
        updated.add(window);
        return new Maid(id, name, locality, serviceOfferings, status, updated, rating, gender);
    }

    public Price calculatePrice(Set<ServiceType> requestedServices) {
        if (requestedServices == null || requestedServices.isEmpty()) {
            throw new IllegalArgumentException("At least one service must be selected");
        }
        Set<ServiceType> offeredServices = serviceOfferings.stream()
                .map(ServiceOffering::serviceType)
                .collect(Collectors.toSet());
        if (!offeredServices.containsAll(requestedServices)) {
            throw new IllegalArgumentException("Requested service is not offered by this maid");
        }
        BigDecimal total = serviceOfferings.stream()
                .filter(offering -> requestedServices.contains(offering.serviceType()))
                .map(offering -> offering.price().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Price(total, serviceOfferings.get(0).price().currency());
    }

    private static void validateUniqueServices(List<ServiceOffering> offerings) {
        Set<ServiceType> serviceTypes = new HashSet<>();
        for (ServiceOffering offering : offerings) {
            if (!serviceTypes.add(offering.serviceType())) {
                throw new IllegalArgumentException("A maid cannot have duplicate service offerings");
            }
        }
    }

    private static void validateSameCurrency(List<ServiceOffering> offerings) {
        String currency = offerings.get(0).price().currency();
        if (offerings.stream().anyMatch(offering -> !currency.equals(offering.price().currency()))) {
            throw new IllegalArgumentException("All service prices must use the same currency");
        }
    }

    public boolean isAvailable(java.time.DayOfWeek day, java.time.LocalTime start, java.time.LocalTime end) {
        return status == MaidStatus.ACTIVE
                && availabilityWindows.stream().anyMatch(window -> window.dayOfWeek() == day
                && window.contains(start, end));
    }

    private static void validateNoOverlappingWindows(List<AvailabilityWindow> windows) {
        for (int i = 0; i < windows.size(); i++) {
            for (int j = i + 1; j < windows.size(); j++) {
                if (windows.get(i).overlaps(windows.get(j))) {
                    throw new IllegalArgumentException("Availability windows must not overlap");
                }
            }
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public MaidId id() { return id; }
    public String name() { return name; }
    public String locality() { return locality; }
    public Set<ServiceType> services() {
        return serviceOfferings.stream().map(ServiceOffering::serviceType).collect(Collectors.toUnmodifiableSet());
    }
    public List<ServiceOffering> serviceOfferings() { return serviceOfferings; }
    public MaidStatus status() { return status; }
    public List<AvailabilityWindow> availabilityWindows() { return availabilityWindows; }
    public double rating() { return rating; }
    public Gender gender() { return gender; }
}
