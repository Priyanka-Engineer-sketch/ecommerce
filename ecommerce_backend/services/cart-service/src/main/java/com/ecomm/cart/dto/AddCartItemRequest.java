package com.ecomm.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull Long productId,
        String sku,
        String name,
        @NotNull @Min(0) Double price,
        @Min(1) int quantity
) {}
