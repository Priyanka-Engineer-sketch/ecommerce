package com.ecomm.events.order;

/**
 * Command to shipping-service to create shipment.
 * Topic: order.saga.commands.shipping
 */
public record ShippingCommand(
        String sagaId,
        Long orderId,
        Long userId
) {}