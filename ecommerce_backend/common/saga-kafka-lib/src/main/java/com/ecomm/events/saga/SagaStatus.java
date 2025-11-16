package com.ecomm.events.saga;

public enum SagaStatus {
    STARTED,
    INVENTORY_RESERVED,
    PAYMENT_AUTHORIZED,
    SHIPPING_CREATED,
    COMPLETED,
    COMPENSATING,
    FAILED
}
