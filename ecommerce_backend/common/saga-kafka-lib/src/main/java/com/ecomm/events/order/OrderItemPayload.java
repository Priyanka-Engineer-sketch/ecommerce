package com.ecomm.events.order;

import java.math.BigDecimal;

/**
 * Lightweight order item snapshot for saga events.
 */
public record OrderItemPayload(
        String productId,
        String name,
        int quantity,
        BigDecimal price
) {}
