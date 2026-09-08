package com.rupeek.maidbooking.maid.domain;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

public final class Maid {
    private final MaidId id;
    private final String name;
    private final String locality;
    private final EnumSet<ServiceType> services;
    private final Price price;
    private final MaidStatus status;
    private final List<AvailabilityWindow> availabilityWindows;

    private Maid(MaidId id, String name, String locality, EnumSet<ServiceType> services,
                 Price price, MaidStatus status, List<AvailabilityWindow> availabilityWindows) {
        this.id = Objects.requireNonNull(id);
        this.name = requireText(name, "Name");
        this.locality = requireText(locality, "Locality");
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("At least one service is required");
        }
        this.services = EnumSet.copyOf(services);
        this.price = Objects.requireNonNull(price);
        this.status = Objects.requireNonNull(status);
        this.availabilityWindows = List.copyOf(availabilityWindows);
        validateNoOverlappingWindows(this.availabilityWindows);
    }

    public static Maid register(String name, String locality, EnumSet<ServiceType> services,
                                Price price, List<AvailabilityWindow> availabilityWindows) {
        if (availabilityWindows == null || availabilityWindows.isEmpty()) {
            throw new IllegalArgumentException("At least one availability window is required");
        }
        return new Maid(MaidId.generate(), name, locality, services, price, MaidStatus.ACTIVE,
                availabilityWindows);
    }

    public Maid addAvailability(AvailabilityWindow window) {
        Objects.requireNonNull(window, "Availability window is required");
        var updated = new ArrayList<>(availabilityWindows);
        updated.add(window);
        return new Maid(id, name, locality, services, price, status, updated);
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
    public EnumSet<ServiceType> services() { return EnumSet.copyOf(services); }
    public Price price() { return price; }
    public MaidStatus status() { return status; }
    public List<AvailabilityWindow> availabilityWindows() { return availabilityWindows; }
}
