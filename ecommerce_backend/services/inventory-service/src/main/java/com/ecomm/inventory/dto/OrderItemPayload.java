package com.ecomm.inventory.dto;

import java.math.BigDecimal;

public record OrderItemPayload(
        String productId,
        String name,
        int quantity,
        BigDecimal price
) {}