package com.ecomm.events.order;

import com.ecomm.events.saga.SagaStatus;

public record OrderSagaReply(
        String sagaId,
        Long orderId,
        SagaStep step,
        SagaStatus status,     // <-- FIXED
        String errorCode,
        String errorMessage,
        long timestamp
) {}
