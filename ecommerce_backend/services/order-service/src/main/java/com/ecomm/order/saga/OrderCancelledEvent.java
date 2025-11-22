package com.ecomm.order.saga;

import com.ecomm.order.domain.OrderStatus;

import java.time.Instant;

public record OrderCancelledEvent(
        String sagaId,
        String orderId,
        OrderStatus previousStatus,
        String reason,
        Instant cancelledAt
) {}
