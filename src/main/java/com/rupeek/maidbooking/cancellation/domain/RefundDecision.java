package com.rupeek.maidbooking.cancellation.domain;

import com.rupeek.maidbooking.maid.domain.Price;

public record RefundDecision(boolean refundable, Price amount, String reason) {
}
