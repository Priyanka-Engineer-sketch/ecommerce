package com.ecomm.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequest(
        @NotEmpty List<OrderItemDto> items
) {
    public record OrderItemDto(
            @NotNull String id,
            @NotNull String name,
            @NotNull Double price,
            @NotNull Integer quantity
    ) {}
}
