package com.ecomm.notification;

import com.ecomm.events.order.domain.OrderStatus;
import com.ecomm.events.payment.PaymentMethod;

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
            Instant createdAt,
            PaymentMethod paymentMethod
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
