package com.ecomm.events.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Command from saga/orchestrator → payment-service.
 */
@Data
@Builder
public class PaymentCommandEvent {

    private String sagaId;

    private Long orderId;
    private Long userId;

    private BigDecimal amount;
    private String currency;

    private PaymentMethod method;

    // Optional fields for different methods:
    private String upiId;
    private String cardLast4;
    private String walletProvider;
}
