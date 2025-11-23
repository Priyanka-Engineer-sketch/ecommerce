package com.ecomm.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InventoryAdjustmentRequest(
        @NotBlank String sku,
        @NotNull Long dataAvailable // positive or negative
) {
}
