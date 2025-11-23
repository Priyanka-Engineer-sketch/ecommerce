package com.ecomm.inventory.dto.response;

public record InventoryResponse(
        String sku,
        Long available,
        Long reserved
) {
}
