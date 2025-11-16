package com.ecomm.order.domain;

public enum OrderStatus {
    PENDING,        // just created, saga not finished
    CONFIRMED,      // inventory + payment + shipping ok
    CANCELLED,      // saga failed, rolled back
    FAILED          // unrecoverable error
}