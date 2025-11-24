package com.ecomm.inventory.web;

import com.ecomm.inventory.dto.request.InventoryAdjustmentRequest;
import com.ecomm.inventory.dto.response.InventoryResponse;
import com.ecomm.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inventory", description = "Stock tracking and adjustments")
@RestController
@RequestMapping("/inventory")   // exposed via gateway as /api/inventory/**
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
            summary = "Get stock for a product",
            description = "Returns current available and reserved quantity for the given SKU.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock info",
                            content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
                    @ApiResponse(responseCode = "404", description = "SKU not found")
            }
    )
    @GetMapping("/{sku}")
    public InventoryResponse getStock(
            @Parameter(description = "Product SKU", example = "SKU-12345")
            @PathVariable String sku
    ) {
        return inventoryService.getBySku(sku);
    }

    @Operation(
            summary = "Adjust stock for a product",
            description = "Admin/Seller stock adjustment. Positive delta increases available stock, negative delta decreases it.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Stock adjusted",
                            content = @Content(schema = @Schema(implementation = InventoryResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid adjustment"),
                    @ApiResponse(responseCode = "404", description = "SKU not found")
            }
    )
    @PostMapping("/adjust")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public InventoryResponse adjustStock(
            @Valid @RequestBody InventoryAdjustmentRequest request
    ) {
        String actorUserId = getCurrentUserId();
        return inventoryService.adjustStock(request, actorUserId);
    }

    // Helper to read authenticated user ID (e.g. subject from JWT)
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return "system"; // or throw if you want strict auth
        }
        return auth.getName();
    }
}
