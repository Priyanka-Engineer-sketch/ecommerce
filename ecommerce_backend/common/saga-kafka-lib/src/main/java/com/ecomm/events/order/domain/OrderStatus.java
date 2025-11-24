package com.ecomm.events.order.domain;

public enum OrderStatus {
    PENDING,        // just created, saga running
    CONFIRMED,      // inventory + payment ok
    READY_TO_SHIP,  // packing done, handover to courier soon
    SHIPPED,
    DELIVERED,
    CANCELLED,
    FAILED          // unrecoverable saga error
}
