package com.rupeek.maidbooking.payment.infrastructure;

import com.rupeek.maidbooking.maid.domain.Price;

public interface RefundGateway {
    GatewayRefundResult refund(String transactionReference, Price amount);
}
