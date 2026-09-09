package com.rupeek.maidbooking.discovery.application;

import com.rupeek.maidbooking.booking.domain.BookingRepository;
import com.rupeek.maidbooking.booking.domain.TimeSlot;
import com.rupeek.maidbooking.maid.domain.Maid;
import com.rupeek.maidbooking.maid.domain.MaidRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaidDiscoveryService {
    private final MaidRepository maidRepository;
    private final BookingRepository bookingRepository;
    private final List<com.rupeek.maidbooking.discovery.domain.MaidFilter> filters;

    public MaidDiscoveryService(MaidRepository maidRepository, BookingRepository bookingRepository,
                                List<com.rupeek.maidbooking.discovery.domain.MaidFilter> filters) {
        this.maidRepository = maidRepository;
        this.bookingRepository = bookingRepository;
        this.filters = filters;
    }

    public List<Maid> search(SearchMaidsQuery query) {
        validateTimeRange(query);
        return maidRepository.findAll().stream()
                .filter(maid -> maid.status() == com.rupeek.maidbooking.maid.domain.MaidStatus.ACTIVE)
                .filter(maid -> filters.stream().allMatch(filter -> filter.matches(maid, query)))
                .filter(maid -> isAvailable(maid, query))
                .toList();
    }

    private boolean isAvailable(Maid maid, SearchMaidsQuery query) {
        if (query.start() == null) return true;
        TimeSlot slot = new TimeSlot(query.start(), query.end());
        return query.start().toLocalDate().equals(query.end().toLocalDate())
                && maid.isAvailable(query.start().getDayOfWeek(), query.start().toLocalTime(), query.end().toLocalTime())
                && !bookingRepository.hasActiveOverlap(maid.id(), slot);
    }

    private static void validateTimeRange(SearchMaidsQuery query) {
        if ((query.start() == null) != (query.end() == null)) {
            throw new IllegalArgumentException("Start and end must be provided together");
        }
        if (query.start() != null && !query.start().isBefore(query.end())) {
            throw new IllegalArgumentException("Search start must be before search end");
        }
    }
}
