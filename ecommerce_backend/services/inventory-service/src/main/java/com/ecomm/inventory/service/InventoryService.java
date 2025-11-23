package com.ecomm.inventory.service;

import com.ecomm.events.order.OrderItemPayload;
import com.ecomm.inventory.dto.request.InventoryAdjustmentRequest;
import com.ecomm.inventory.dto.response.InventoryResponse;

import java.util.List;

public interface InventoryService {

    InventoryResponse getBySku(String sku);

    InventoryResponse adjustStock(InventoryAdjustmentRequest request, String actorUserId);

    boolean reserveForOrder(Long orderId, List<OrderItemPayload> payload);

    void releaseForOrder(Long orderId);
}
