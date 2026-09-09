package com.rupeek.maidbooking.discovery.domain;

import com.rupeek.maidbooking.discovery.application.SearchMaidsQuery;
import com.rupeek.maidbooking.maid.domain.Maid;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PriceFilter implements MaidFilter {
    public boolean matches(Maid maid, SearchMaidsQuery query) {
        if (query.maxPrice() == null) return true;
        BigDecimal price = query.services() == null || query.services().isEmpty()
                ? maid.serviceOfferings().stream().map(offering -> offering.price().amount())
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO)
                : selectedPrice(maid, query);
        return price.compareTo(query.maxPrice()) <= 0;
    }

    private BigDecimal selectedPrice(Maid maid, SearchMaidsQuery query) {
        try {
            return maid.calculatePrice(query.services()).amount();
        } catch (IllegalArgumentException exception) {
            return BigDecimal.valueOf(Long.MAX_VALUE);
        }
    }
}
