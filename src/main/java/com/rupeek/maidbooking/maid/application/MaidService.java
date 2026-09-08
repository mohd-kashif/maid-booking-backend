package com.rupeek.maidbooking.maid.application;

import com.rupeek.maidbooking.maid.domain.AvailabilityWindow;
import com.rupeek.maidbooking.maid.domain.Maid;
import com.rupeek.maidbooking.maid.domain.MaidId;
import com.rupeek.maidbooking.maid.domain.MaidRepository;
import com.rupeek.maidbooking.maid.domain.Price;
import com.rupeek.maidbooking.maid.domain.ServiceType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
public class MaidService {
    private final MaidRepository repository;

    public MaidService(MaidRepository repository) {
        this.repository = repository;
    }

    public Maid register(String name, String locality, EnumSet<ServiceType> services,
                         Price price,
                         List<AvailabilityWindow> availabilityWindows) {
        return repository.save(Maid.register(name, locality, services, price, availabilityWindows));
    }

    public Maid get(UUID maidId) {
        return repository.findById(new MaidId(maidId))
                .orElseThrow(() -> new MaidNotFoundException(maidId));
    }

    public Maid addAvailability(UUID maidId, AvailabilityWindow window) {
        Maid maid = get(maidId);
        Maid updated = maid.addAvailability(window);
        return repository.update(updated);
    }

    public boolean isAvailable(UUID maidId, DayOfWeek day, LocalTime start, LocalTime end) {
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Requested start time must be before end time");
        }
        return get(maidId).isAvailable(day, start, end);
    }

    public static class MaidNotFoundException extends RuntimeException {
        public MaidNotFoundException(UUID maidId) {
            super("Maid not found: " + maidId);
        }
    }
}
