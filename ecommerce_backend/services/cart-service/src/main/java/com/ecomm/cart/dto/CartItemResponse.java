package com.ecomm.cart.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String sku,
        String name,
        Double price,
        int quantity
) {}
