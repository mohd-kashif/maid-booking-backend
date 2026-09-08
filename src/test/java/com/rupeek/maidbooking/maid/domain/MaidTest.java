package com.rupeek.maidbooking.maid.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MaidTest {

    @Test
    void registersMaidWithValidDetails() {
        Maid maid = Maid.register("Asha", "Indiranagar", List.of(offering(ServiceType.CLEANING, 500)),
                List.of(window(9, 13)));

        assertEquals("Asha", maid.name());
        assertTrue(maid.isAvailable(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(12, 0)));
    }

    @Test
    void calculatesTotalForSelectedServices() {
        Maid maid = Maid.register("Asha", "Indiranagar", List.of(
                        offering(ServiceType.CLEANING, 400), offering(ServiceType.COOKING, 600)),
                List.of(window(9, 13)));

        assertEquals(BigDecimal.valueOf(1000), maid.calculatePrice(
                Set.of(ServiceType.CLEANING, ServiceType.COOKING)).amount());
    }

    @Test
    void rejectsServiceThatMaidDoesNotOffer() {
        Maid maid = Maid.register("Asha", "Indiranagar", List.of(offering(ServiceType.CLEANING, 400)),
                List.of(window(9, 13)));

        assertThrows(IllegalArgumentException.class,
                () -> maid.calculatePrice(Set.of(ServiceType.COOKING)));
    }

    @Test
    void rejectsOverlappingWindows() {
        assertThrows(IllegalArgumentException.class, () -> Maid.register("Asha", "Indiranagar",
                List.of(offering(ServiceType.CLEANING, 500)),
                List.of(window(9, 13), window(12, 15))));
    }

    @Test
    void rejectsInvalidPrice() {
        assertThrows(IllegalArgumentException.class,
                () -> new Price(BigDecimal.ZERO, "INR"));
    }

    @Test
    void rejectsRequestOutsideAvailabilityWindow() {
        Maid maid = Maid.register("Asha", "Indiranagar", List.of(offering(ServiceType.CLEANING, 500)),
                List.of(window(9, 13)));

        assertFalse(maid.isAvailable(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)));
    }

    private static AvailabilityWindow window(int start, int end) {
        return new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(start, 0), LocalTime.of(end, 0));
    }

    private static ServiceOffering offering(ServiceType type, int amount) {
        return new ServiceOffering(type, new Price(BigDecimal.valueOf(amount), "INR"));
    }
}
