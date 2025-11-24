package com.ecomm.events.order;

import com.ecomm.events.saga.SagaStatus;

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
        SagaStatus status,
        String errorMessage
) {}