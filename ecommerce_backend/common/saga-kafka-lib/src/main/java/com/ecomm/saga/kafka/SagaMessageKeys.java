package com.ecomm.saga.kafka;

import com.ecomm.events.order.SagaStep;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public final class SagaMessageKeys {

    private static final String DELIMITER = ":";   // safer than '|'

    private SagaMessageKeys() {}

    /**
     * Produces a stable, safe, idempotent Kafka message key.
     *
     * Format:
     *   <base64(sagaId)>:<STEP>
     *
     * Example:
     *   QUJDMTIzNDU=:INVENTORY
     */
    public static String commandKey(String sagaId, SagaStep step) {

        // Validate inputs clearly
        Objects.requireNonNull(sagaId, "sagaId must not be null");
        Objects.requireNonNull(step, "SagaStep must not be null");

        if (sagaId.isBlank()) {
            throw new IllegalArgumentException("sagaId must not be blank");
        }

        // Normalize + make key safe for Kafka partitioning
        String safeSagaId = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sagaId.getBytes(StandardCharsets.UTF_8));

        return safeSagaId + DELIMITER + step.name();
    }
}
