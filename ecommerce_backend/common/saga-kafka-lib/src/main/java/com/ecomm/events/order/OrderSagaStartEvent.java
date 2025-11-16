package com.ecomm.events.order;

import java.math.BigDecimal;
import java.util.List;

/**
 * Published by order-service to kick off the saga.
 * Topic: order.saga.start
 */
public record OrderSagaStartEvent(
        String sagaId,
        String orderId,      // external order id, e.g. "O-12345"
        String customerId,
        BigDecimal totalAmount,
        List<OrderItemPayload> items
) {}
