package com.rupeek.maidbooking.maid.infrastructure;

import com.rupeek.maidbooking.maid.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryMaidRepositoryTest {
    @Test
    void rejectsSavingSameMaidTwice() {
        var repository = new InMemoryMaidRepository();
        var maid = Maid.register("Asha", "Indiranagar", EnumSet.of(ServiceType.CLEANING),
                new Price(BigDecimal.valueOf(500), "INR"),
                List.of(new AvailabilityWindow(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0))));

        repository.save(maid);
        assertThrows(IllegalStateException.class, () -> repository.save(maid));
    }
}
