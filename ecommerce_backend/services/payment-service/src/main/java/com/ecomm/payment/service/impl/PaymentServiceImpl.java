package com.ecomm.payment.service.impl;

import com.ecomm.events.payment.PaymentStatus;
import com.ecomm.payment.domain.Payment;
import com.ecomm.payment.dto.ProcessPaymentRequest;
import com.ecomm.payment.repository.PaymentRepository;
import com.ecomm.payment.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    @Override
    public Payment processPayment(ProcessPaymentRequest req) {

        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .userId(req.getUserId())
                .amount(req.getAmount())
                .currency(req.getCurrency())
                .method(req.getMethod())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        payment = paymentRepository.save(payment);

        try {
            switch (req.getMethod()) {
                case CARD -> simulateCardPayment(payment, req);
                case UPI -> simulateUpiPayment(payment, req);
                case NET_BANKING -> simulateNetBanking(payment, req);
                case WALLET -> simulateWalletPayment(payment, req);
                case COD -> handleCashOnDelivery(payment, req);
            }
        } catch (Exception ex) {
            log.error("Payment gateway error for order {}: {}", req.getOrderId(), ex.getMessage(), ex);
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(ex.getMessage());
        }

        payment.setUpdatedAt(Instant.now());
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for orderId: " + orderId));
    }

    // ----- simulation methods; replace with real gateway integration later -----

    private void simulateCardPayment(Payment payment, ProcessPaymentRequest req) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("CARD-" + UUID.randomUUID());
        log.info("Simulated CARD payment for order {} amount {}", req.getOrderId(), req.getAmount());
    }

    private void simulateUpiPayment(Payment payment, ProcessPaymentRequest req) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("UPI-" + UUID.randomUUID());
        log.info("Simulated UPI payment for order {} amount {}", req.getOrderId(), req.getAmount());
    }

    private void simulateNetBanking(Payment payment, ProcessPaymentRequest req) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("NB-" + UUID.randomUUID());
        log.info("Simulated NET_BANKING payment for order {} amount {}", req.getOrderId(), req.getAmount());
    }

    private void simulateWalletPayment(Payment payment, ProcessPaymentRequest req) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setTransactionRef("WALLET-" + UUID.randomUUID());
        log.info("Simulated WALLET payment for order {} amount {}", req.getOrderId(), req.getAmount());
    }

    private void handleCashOnDelivery(Payment payment, ProcessPaymentRequest req) {
        payment.setStatus(PaymentStatus.PENDING); // COD will be settled later
        payment.setTransactionRef("COD-" + UUID.randomUUID());
        log.info("Handled COD payment for order {} amount {}", req.getOrderId(), req.getAmount());
    }
}
