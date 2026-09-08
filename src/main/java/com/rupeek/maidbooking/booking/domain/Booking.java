package com.rupeek.maidbooking.booking.domain;

import com.rupeek.maidbooking.maid.domain.MaidId;
import com.rupeek.maidbooking.maid.domain.Price;
import com.rupeek.maidbooking.maid.domain.ServiceType;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Booking {
    private final BookingId id;
    private final String customerId;
    private final MaidId maidId;
    private final BookingType type;
    private final Set<ServiceType> services;
    private final Price priceSnapshot;
    private final List<TimeSlot> slots;
    private BookingStatus status;

    private Booking(BookingId id, String customerId, MaidId maidId, BookingType type,
                    Set<ServiceType> services, Price priceSnapshot, List<TimeSlot> slots) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer id must not be blank");
        }
        if (services == null || services.isEmpty()) {
            throw new IllegalArgumentException("At least one service is required");
        }
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("At least one time slot is required");
        }
        this.id = Objects.requireNonNull(id);
        this.customerId = customerId;
        this.maidId = Objects.requireNonNull(maidId);
        this.type = Objects.requireNonNull(type);
        this.services = Set.copyOf(services);
        this.priceSnapshot = Objects.requireNonNull(priceSnapshot);
        this.slots = List.copyOf(slots);
        this.status = BookingStatus.CONFIRMED;
    }

    public static Booking create(String customerId, MaidId maidId, BookingType type,
                                 Set<ServiceType> services, Price price, List<TimeSlot> slots) {
        return new Booking(BookingId.generate(), customerId, maidId, type, services, price, slots);
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled");
        }
        status = BookingStatus.CANCELLED;
    }

    public boolean isActive() { return status == BookingStatus.CONFIRMED; }
    public BookingId id() { return id; }
    public UUID bookingId() { return id.value(); }
    public String customerId() { return customerId; }
    public MaidId maidId() { return maidId; }
    public BookingType type() { return type; }
    public Set<ServiceType> services() { return services; }
    public Price priceSnapshot() { return priceSnapshot; }
    public List<TimeSlot> slots() { return slots; }
    public BookingStatus status() { return status; }
}
