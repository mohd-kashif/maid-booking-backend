package com.rupeek.maidbooking.discovery.domain;

import com.rupeek.maidbooking.discovery.application.SearchMaidsQuery;
import com.rupeek.maidbooking.maid.domain.Maid;
import org.springframework.stereotype.Component;

@Component
public class RatingFilter implements MaidFilter {
    public boolean matches(Maid maid, SearchMaidsQuery query) {
        return query.minimumRating() == null || maid.rating() >= query.minimumRating();
    }
}
