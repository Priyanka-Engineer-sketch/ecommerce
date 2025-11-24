package com.ecomm.inventory.service;

import com.ecomm.events.order.OrderItemPayload;
import com.ecomm.inventory.dto.request.InventoryAdjustmentRequest;
import com.ecomm.inventory.dto.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    /**
     * Get current inventory for a SKU (cached).
     */
    InventoryResponse getBySku(String sku);

    /**
     * Admin/Seller stock adjustment.
     */
    InventoryResponse adjustStock(InventoryAdjustmentRequest request, String actorUserId);

    /**
     * Saga: reserve stock for an order (called by Kafka saga handler).
     */
    boolean reserveForOrder(Long orderId, List<OrderItemPayload> payload);

    /**
     * Saga: release reserved stock for an order (compensation).
     */
    void releaseForOrder(Long orderId);
}
