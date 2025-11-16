package com.ecomm.events.order;

/**
 * Generic reply from any saga participant (inventory, payment, shipping)
 * back to the orchestrator.
 *
 * Topic: order.saga.replies
 */
public record SagaReplyEvent(
        String sagaId,
        Long orderId,
        SagaStep step,
        boolean success,
        String errorMessage
) {}