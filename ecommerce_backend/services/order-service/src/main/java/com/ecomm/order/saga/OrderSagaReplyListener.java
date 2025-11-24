package com.ecomm.order.saga;

import com.ecomm.events.inventory.InventoryResultEvent;
import com.ecomm.events.order.SagaStep;
import com.ecomm.events.payment.PaymentCommandEvent;
import com.ecomm.events.payment.PaymentResultEvent;
import com.ecomm.events.order.domain.Order;
import com.ecomm.events.order.domain.OrderStatus;
import com.ecomm.order.repository.OrderRepository;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaReplyListener {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> sagaKafkaTemplate;

    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_REPLIES,
            groupId = "order-service-saga"
    )
    public void handleSagaReply(Object payload) {

        if (payload instanceof InventoryResultEvent inventoryResult) {
            handleInventoryResult(inventoryResult);
        } else if (payload instanceof PaymentResultEvent paymentResult) {
            handlePaymentResult(paymentResult);
        }
    }

    // ------------- STEP 1: inventory result -> trigger payment -------------
    private void handleInventoryResult(InventoryResultEvent event) {
        log.info("Received InventoryResultEvent: {}", event);

        Optional<Order> opt = orderRepository.findById(event.getOrderId());
        if (opt.isEmpty()) {
            log.warn("Order not found for InventoryResultEvent, orderId={}", event.getOrderId());
            return;
        }

        Order order = opt.get();

        if (!event.isSuccess()) {
            // Inventory failed → cancel order
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            log.info("Order {} inventory FAILED, status → CANCELLED", order.getId());
            return;
        }

        // Inventory success → send payment command
        log.info("Order {} inventory SUCCESS, triggering PAYMENT step in saga", order.getId());
        sendPaymentCommand(event.getSagaId(), order);
    }

    // ------------- STEP 2: payment result -> update order status -----------
    private void handlePaymentResult(PaymentResultEvent event) {
        log.info("Received PaymentResultEvent: {}", event);

        Optional<Order> opt = orderRepository.findById(event.getOrderId());
        if (opt.isEmpty()) {
            log.warn("Order not found for PaymentResultEvent, orderId={}", event.getOrderId());
            return;
        }

        Order order = opt.get();

        switch (event.getStatus()) {
            case SUCCESS -> {
                order.setStatus(OrderStatus.CONFIRMED);
                orderRepository.save(order);
                log.info("Order {} payment SUCCESS, status → CONFIRMED", order.getId());

                // TODO: now trigger SHIPPING saga step via order.saga.commands.shipping
                // sendShippingCommand(event.getSagaId(), order);
            }
            case FAILED -> {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                log.info("Order {} payment FAILED, status → CANCELLED", order.getId());
                // TODO: optionally trigger inventory compensation here
            }
            case PENDING -> {
                // COD scenario
                log.info("Order {} payment PENDING (COD). Keeping status {}", order.getId(), order.getStatus());
            }
        }
    }

    // ------------- Helper: send PaymentCommandEvent ------------------------
    public void sendPaymentCommand(String sagaId, Order order) {

        PaymentCommandEvent cmd = PaymentCommandEvent.builder()
                .sagaId(sagaId)
                .orderId(order.getId())
                .userId(Long.valueOf(order.getCustomer().getCustomerId()))
                .amount(BigDecimal.valueOf(order.getTotalAmount()))
                .currency("INR") // or order.getCurrency()
                .method(order.getPaymentMethod())
                .build();

        String key = SagaMessageKeys.commandKey(sagaId, SagaStep.PAYMENT);
        sagaKafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_COMMAND_PAYMENT, key, cmd);

        log.info("Sent PaymentCommandEvent for order {} saga {}", order.getId(), sagaId);
    }
}
