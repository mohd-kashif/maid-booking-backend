package com.rupeek.maidbooking.maid.domain;

import java.util.Objects;
import java.util.UUID;

public record MaidId(UUID value) {
    public MaidId {
        Objects.requireNonNull(value, "Maid id is required");
    }

    public static MaidId generate() {
        return new MaidId(UUID.randomUUID());
    }
}
