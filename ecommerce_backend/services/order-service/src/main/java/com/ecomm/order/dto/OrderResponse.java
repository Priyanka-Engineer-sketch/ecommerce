package com.ecomm.order.dto;

import com.ecomm.events.order.domain.OrderStatus;
import com.ecomm.events.order.domain.RecommendedProductSummary;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,   // externalOrderId
        CreateOrderRequest.CustomerDto customer,
        CreateOrderRequest.ShippingAddressDto shippingAddress,
        List<CreateOrderRequest.OrderItemDto> items,
        double totalAmount,
        OrderStatus status,
        Instant createdAt,
        List<RecommendedProductSummary> recommendedProducts
) {}
