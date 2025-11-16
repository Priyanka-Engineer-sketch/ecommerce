package com.ecomm.order.saga;

import com.ecomm.events.order.OrderItemPayload;
import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.order.domain.Order;
import com.ecomm.order.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class OrderSagaMapper {

    public static OrderSagaStartEvent toStartEvent(String sagaId, Order order) {
        List<OrderItemPayload> items = order.getItems().stream()
                .map(OrderSagaMapper::toItemPayload)
                .toList();

        String customerId = order.getCustomer() != null
                ? order.getCustomer().getCustomerId()
                : null;

        return new OrderSagaStartEvent(
                sagaId,
                order.getExternalOrderId(),
                customerId,
                BigDecimal.valueOf(order.getTotalAmount()),
                items
        );
    }

    private static OrderItemPayload toItemPayload(OrderItem item) {
        return new OrderItemPayload(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                BigDecimal.valueOf(item.getPrice())
        );
    }

    public static String newSagaId() {
        return UUID.randomUUID().toString();
    }
}
