package com.rupeek.maidbooking.maid.domain;

import java.util.Optional;

public interface MaidRepository {
    Maid save(Maid maid);
    Maid update(Maid maid);
    Optional<Maid> findById(MaidId id);
}
