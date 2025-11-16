package com.ecomm.events.order;

import java.math.BigDecimal;

/**
 * Command to payment-service to authorize/capture payment.
 * Topic: order.saga.commands.payment
 */
public record PaymentCommand(
        String sagaId,
        Long orderId,
        Long userId,
        BigDecimal amount
) {}