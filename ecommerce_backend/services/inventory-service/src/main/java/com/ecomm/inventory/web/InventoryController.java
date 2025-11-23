package com.ecomm.inventory.web;


import com.ecomm.inventory.dto.request.InventoryAdjustmentRequest;
import com.ecomm.inventory.dto.response.InventoryResponse;
import com.ecomm.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    public InventoryResponse get(@PathVariable String sku) {
        return inventoryService.getBySku(sku);
    }

    @PostMapping("/adjust")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public InventoryResponse adjust(@Valid @RequestBody InventoryAdjustmentRequest request,
                                    Authentication auth) {
        String userId = auth != null ? auth.getName() : "unknown";
        return inventoryService.adjustStock(request, userId);
    }
}
