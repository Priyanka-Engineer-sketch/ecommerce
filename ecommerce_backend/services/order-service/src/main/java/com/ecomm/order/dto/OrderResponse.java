package com.ecomm.order.dto;

import com.ecomm.order.domain.OrderStatus;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,   // externalOrderId
        CreateOrderRequest.CustomerDto customer,
        CreateOrderRequest.ShippingAddressDto shippingAddress,
        List<CreateOrderRequest.OrderItemDto> items,
        double totalAmount,
        OrderStatus status,
        Instant createdAt
) {}
