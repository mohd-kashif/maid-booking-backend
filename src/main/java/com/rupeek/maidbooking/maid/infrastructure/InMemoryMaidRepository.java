package com.rupeek.maidbooking.maid.infrastructure;

import com.rupeek.maidbooking.maid.domain.Maid;
import com.rupeek.maidbooking.maid.domain.MaidId;
import com.rupeek.maidbooking.maid.domain.MaidRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryMaidRepository implements MaidRepository {
    private final ConcurrentMap<MaidId, Maid> maids = new ConcurrentHashMap<>();

    @Override
    public Maid save(Maid maid) {
        var existing = maids.putIfAbsent(maid.id(), maid);
        if (existing != null) {
            throw new IllegalStateException("Maid already exists: " + maid.id().value());
        }
        return maid;
    }

    public Maid update(Maid maid) {
        if (!maids.replace(maid.id(), maids.get(maid.id()), maid)) {
            throw new IllegalStateException("Maid does not exist: " + maid.id().value());
        }
        return maid;
    }

    @Override
    public Optional<Maid> findById(MaidId id) {
        return Optional.ofNullable(maids.get(id));
    }
}
