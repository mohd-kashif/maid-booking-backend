package com.rupeek.maidbooking.discovery.application;

import com.rupeek.maidbooking.maid.domain.Gender;
import com.rupeek.maidbooking.maid.domain.ServiceType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

public record SearchMaidsQuery(String locality, Set<ServiceType> services,
                               OffsetDateTime start, OffsetDateTime end,
                               BigDecimal maxPrice, Double minimumRating,
                               Gender gender) {}
