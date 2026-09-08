package com.rupeek.maidbooking.maid.domain;

import java.util.Objects;

public record ServiceOffering(ServiceType serviceType, Price price) {
    public ServiceOffering {
        Objects.requireNonNull(serviceType, "Service type is required");
        Objects.requireNonNull(price, "Service price is required");
    }
}
