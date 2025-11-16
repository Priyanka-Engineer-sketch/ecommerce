package com.ecomm.events.order;


import com.ecomm.events.saga.SagaStatus;

/**
 * Sent by the saga orchestrator to inform order-service
 * of the final saga outcome (COMPLETED, FAILED, COMPENSATING, etc.).
 *
 * You can put this on its own topic, e.g. order.saga.result
 */
public record OrderSagaResultEvent(
        String sagaId,
        Long orderId,
        SagaStatus status,
        String errorMessage
) {}
