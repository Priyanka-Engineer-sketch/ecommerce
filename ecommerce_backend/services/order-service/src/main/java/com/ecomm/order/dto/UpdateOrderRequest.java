package com.ecomm.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequest(
        @NotNull CreateOrderRequest.ShippingAddressDto shippingAddress,
        @NotEmpty List<CreateOrderRequest.OrderItemDto> items
) {}
