package com.ecomm.cart.dto;

import java.util.List;

public record CartResponse(
        Long cartId,
        String userId,
        List<CartItemResponse> items,
        int totalItems,
        Double totalAmount
) {}
