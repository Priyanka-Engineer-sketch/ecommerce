package com.ecomm.payment.service;

import com.ecomm.payment.domain.Payment;
import com.ecomm.payment.dto.ProcessPaymentRequest;

public interface PaymentService {

    Payment processPayment(ProcessPaymentRequest request);

    Payment getByOrderId(Long orderId);
}
