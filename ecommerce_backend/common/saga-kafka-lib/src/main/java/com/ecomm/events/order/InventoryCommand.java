package com.ecomm.events.order;

import java.util.List;

/**
 * Command to inventory-service to reserve items.
 * Topic: order.saga.commands.inventory
 */
public record InventoryCommand(
        String sagaId,
        String orderId,
        List<OrderItemPayload> items
) {}