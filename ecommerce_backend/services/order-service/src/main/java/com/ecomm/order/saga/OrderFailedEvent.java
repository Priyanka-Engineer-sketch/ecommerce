package com.ecomm.order.saga;

import java.time.Instant;

public record OrderFailedEvent(
        String sagaId,
        String orderId,
        String reason,
        Instant failedAt
) {}
