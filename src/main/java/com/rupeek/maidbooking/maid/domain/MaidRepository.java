package com.rupeek.maidbooking.maid.domain;

import java.util.Optional;
import java.util.List;

public interface MaidRepository {
    Maid save(Maid maid);
    Maid update(Maid maid);
    Optional<Maid> findById(MaidId id);
    List<Maid> findAll();
}
