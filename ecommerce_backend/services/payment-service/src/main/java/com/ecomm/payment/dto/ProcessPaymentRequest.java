package com.ecomm.payment.dto;

import com.ecomm.events.payment.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProcessPaymentRequest {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;

    private String upiId;
    private String cardLast4;
    private String walletProvider;
}
