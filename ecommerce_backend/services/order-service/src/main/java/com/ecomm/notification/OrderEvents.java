package com.ecomm.notification;

import com.ecomm.order.domain.OrderStatus;

import java.time.Instant;

public class OrderEvents {

    public record OrderPlacedEvent(
            Long orderId,
            String externalOrderId,
            String customerId,
            String customerName,
            String customerEmail,
            double totalAmount,
            OrderStatus status,
            Instant createdAt
    ) {
    }

    public record OrderCancelledEvent(
            Long orderId,
            String externalOrderId,
            String customerId,
            String customerName,
            String customerEmail,
            double totalAmount,
            OrderStatus previousStatus,
            Instant cancelledAt
    ) {
    }
}
