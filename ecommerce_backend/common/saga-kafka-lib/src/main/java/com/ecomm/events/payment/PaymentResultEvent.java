package com.ecomm.events.payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Result from payment-service → saga/orchestrator (via order.saga.replies).
 */
@Data
@Builder
public class PaymentResultEvent {

    private String sagaId;

    private Long orderId;
    private Long userId;

    private BigDecimal amount;
    private String currency;

    private PaymentMethod method;
    private PaymentStatus status;

    private String transactionRef;
    private String failureReason;
}
