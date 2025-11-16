package com.ecomm.order.dto;

import com.ecomm.order.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String orderId,
        CreateOrderRequest.CustomerDto customer,
        List<CreateOrderRequest.OrderItemDto> items,
        double totalAmount,
        OrderStatus status,
        Instant createdAt
) {}
