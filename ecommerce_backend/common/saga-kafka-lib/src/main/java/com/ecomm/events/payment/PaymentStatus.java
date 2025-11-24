package com.ecomm.events.payment;

/**
 * Payment status used in saga events across services.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
