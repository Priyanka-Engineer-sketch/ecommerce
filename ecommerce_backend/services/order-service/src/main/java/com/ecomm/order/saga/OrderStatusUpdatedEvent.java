package com.ecomm.order.saga;

import com.ecomm.events.order.domain.OrderStatus;

import java.time.Instant;

public record OrderStatusUpdatedEvent(
        String sagaId,
        String orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant updatedAt
) {}
