package com.ecomm.payment.kafka;

import com.ecomm.events.payment.PaymentCommandEvent;
import com.ecomm.events.payment.PaymentResultEvent;
import com.ecomm.events.payment.PaymentStatus;
import com.ecomm.payment.domain.Payment;
import com.ecomm.payment.dto.ProcessPaymentRequest;
import com.ecomm.payment.service.PaymentService;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSagaListener {

    private final PaymentService paymentService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_COMMAND_PAYMENT,
            groupId = "payment-service"
    )
    public void handlePaymentCommand(PaymentCommandEvent event) {
        log.info("Received PaymentCommandEvent: {}", event);

        try {
            ProcessPaymentRequest req = ProcessPaymentRequest.builder()
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .method(event.getMethod())
                    .upiId(event.getUpiId())
                    .cardLast4(event.getCardLast4())
                    .walletProvider(event.getWalletProvider())
                    .build();

            Payment payment = paymentService.processPayment(req);

            PaymentResultEvent result = PaymentResultEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .method(payment.getMethod())
                    .status(payment.getStatus())
                    .transactionRef(payment.getTransactionRef())
                    .failureReason(payment.getFailureReason())
                    .build();

            kafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_REPLIES, result.getSagaId(), result);
            log.info("Sent PaymentResultEvent: {}", result);

        } catch (Exception ex) {
            log.error("Error processing payment for order {}: {}", event.getOrderId(), ex.getMessage(), ex);

            PaymentResultEvent result = PaymentResultEvent.builder()
                    .sagaId(event.getSagaId())
                    .orderId(event.getOrderId())
                    .userId(event.getUserId())
                    .amount(event.getAmount())
                    .currency(event.getCurrency())
                    .method(event.getMethod())
                    .status(PaymentStatus.FAILED)
                    .failureReason(ex.getMessage())
                    .build();

            kafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_REPLIES, result.getSagaId(), result);
        }
    }
}
