package com.ecomm.order.saga;

import com.ecomm.order.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderStatusUpdatedEvent(
        String sagaId,
        String orderId,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        Instant updatedAt
) {}
