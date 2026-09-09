package com.rupeek.maidbooking.discovery.domain;

import com.rupeek.maidbooking.discovery.application.SearchMaidsQuery;
import com.rupeek.maidbooking.maid.domain.Maid;

public interface MaidFilter {
    boolean matches(Maid maid, SearchMaidsQuery query);
}
