package com.ecomm.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateOrderRequest(
        @NotNull CustomerDto customer,
        @NotEmpty List<OrderItemDto> items
) {
    public record CustomerDto(
            @NotNull String id,
            @NotNull String name,
            @NotNull String email
    ) {}

    public record OrderItemDto(
            @NotNull String id,
            @NotNull String name,
            @NotNull Double price,
            @NotNull Integer quantity
    ) {}
}
