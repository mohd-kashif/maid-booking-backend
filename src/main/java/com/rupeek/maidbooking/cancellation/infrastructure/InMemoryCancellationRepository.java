package com.rupeek.maidbooking.cancellation.infrastructure;

import com.rupeek.maidbooking.cancellation.domain.Cancellation;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCancellationRepository {
    private final Map<UUID, Cancellation> cancellations = new ConcurrentHashMap<>();

    public Cancellation save(Cancellation cancellation) {
        cancellations.put(cancellation.id(), cancellation);
        return cancellation;
    }
}
