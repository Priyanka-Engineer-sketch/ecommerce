package com.ecomm.order.service;

import com.ecomm.events.order.*;
import com.ecomm.events.order.domain.Order;
import com.ecomm.events.order.domain.OrderStatus;
import com.ecomm.events.saga.SagaStatus;
import com.ecomm.ordersaga.domain.OrderSagaEntity;
import com.ecomm.ordersaga.domain.repository.OrderSagaRepository;
import com.ecomm.order.repository.OrderRepository;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service("orderSagaLibService") // <-- this matches your @Qualifier
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> sagaKafkaTemplate;
    private final OrderRepository orderRepository;

    // =========================================================
    // START SAGA
    // =========================================================
    @Transactional
    public void startSaga(OrderSagaStartEvent event) {
        // 1. Persist initial saga state
        OrderSagaEntity saga = new OrderSagaEntity();
        saga.setSagaId(event.sagaId());
        saga.setOrderId(Long.valueOf(event.orderId()));
        saga.setStatus(SagaStatus.STARTED);
        saga.setInventoryDone(false);
        saga.setPaymentDone(false);
        saga.setShippingDone(false);
        sagaRepository.save(saga);

        // 2. First step: send inventory command
        InventoryCommand cmd = new InventoryCommand(
                event.sagaId(),
                event.orderId(),
                event.items()
        );

        String key = SagaMessageKeys.commandKey(event.sagaId(), SagaStep.INVENTORY);

        sagaKafkaTemplate.send(
                SagaKafkaTopics.ORDER_SAGA_CMD_INVENTORY,
                key,
                cmd
        );

        log.info("SAGA [{}] started for order {}", event.sagaId(), event.orderId());
    }

    // =========================================================
    // REPLY HANDLING – ORCHESTRATION + ORDER STATUS
    // =========================================================

    /**
     * Kafka listener for saga replies.
     * Adjust the payload type (SagaReplyEvent / OrderSagaReply) to what you actually use.
     */
    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_REPLIES,
            groupId = "order-service-saga"
    )
    @Transactional
    public void onSagaReply(@Payload SagaReplyEvent reply) {
        log.info("Order-service received saga reply: {}", reply);

        // 1) Update saga entity & send next commands
        handleReply(reply);

        // 2) Update Order aggregate status
        Order order = orderRepository.findById(reply.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for saga reply, id=" + reply.orderId()
                ));

        applyStatusTransition(order, reply);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    @Transactional
    public void handleReply(SagaReplyEvent reply) {
        OrderSagaEntity saga = sagaRepository.findById(reply.sagaId())
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + reply.sagaId()));

        // Failure path → mark compensating
        if (!reply.success()) {
            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setLastError(reply.errorMessage());
            sagaRepository.save(saga);
            // TODO: send compensation commands (reverse inventory / refund payment)
            log.warn("SAGA [{}] compensating due to error: {}",
                    reply.sagaId(), reply.errorMessage());
            return;
        }

        // Success path per step
        switch (reply.step()) {
            case INVENTORY -> handleInventorySuccess(saga, reply);
            case PAYMENT   -> handlePaymentSuccess(saga, reply);
            case SHIPPING  -> handleShippingSuccess(saga, reply);
        }
    }

    private void handleInventorySuccess(OrderSagaEntity saga, SagaReplyEvent reply) {
        saga.setInventoryDone(true);
        saga.setStatus(SagaStatus.INVENTORY_RESERVED);
        sagaRepository.save(saga);

        // send payment command
        PaymentCommand cmd = new PaymentCommand(
                saga.getSagaId(),
                saga.getOrderId(),
                null, // TODO: userId
                null  // TODO: amount
        );

        String key = SagaMessageKeys.commandKey(saga.getSagaId(), SagaStep.PAYMENT);

        sagaKafkaTemplate.send(
                SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT,
                key,
                cmd
        );
        log.info("SAGA [{}] INVENTORY done → PAYMENT command sent", saga.getSagaId());
    }

    private void handlePaymentSuccess(OrderSagaEntity saga, SagaReplyEvent reply) {
        saga.setPaymentDone(true);
        saga.setStatus(SagaStatus.PAYMENT_AUTHORIZED);
        sagaRepository.save(saga);

        ShippingCommand cmd = new ShippingCommand(
                saga.getSagaId(),
                saga.getOrderId(),
                null // TODO: userId if needed
        );

        String key = SagaMessageKeys.commandKey(saga.getSagaId(), SagaStep.SHIPPING);

        sagaKafkaTemplate.send(
                SagaKafkaTopics.ORDER_SAGA_CMD_SHIPPING,
                key,
                cmd
        );
        log.info("SAGA [{}] PAYMENT done → SHIPPING command sent", saga.getSagaId());
    }

    private void handleShippingSuccess(OrderSagaEntity saga, SagaReplyEvent reply) {
        saga.setShippingDone(true);
        saga.setStatus(SagaStatus.COMPLETED);
        sagaRepository.save(saga);
        log.info("SAGA [{}] completed for order {}", saga.getSagaId(), saga.getOrderId());

        // Optionally: send a final result event back
        // sagaKafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_RESULT, ..., ...);
    }

    // =========================================================
    // ORDER STATUS MAPPING
    // =========================================================
    private void applyStatusTransition(Order order, SagaReplyEvent reply) {

        SagaStatus status = reply.status();
        SagaStep step = reply.step();

        // happy path
        if (status == SagaStatus.INVENTORY_RESERVED && step == SagaStep.INVENTORY) {
            order.setStatus(OrderStatus.CONFIRMED);
            return;
        }

        if (status == SagaStatus.PAYMENT_AUTHORIZED && step == SagaStep.PAYMENT) {
            order.setStatus(OrderStatus.READY_TO_SHIP);
            return;
        }

        if (status == SagaStatus.SHIPPING_CREATED && step == SagaStep.SHIPPING) {
            order.setStatus(OrderStatus.SHIPPED);
            return;
        }

        if (status == SagaStatus.COMPLETED) {
            order.setStatus(OrderStatus.DELIVERED);
            return;
        }

        // failure / compensation → cancel
        if (status == SagaStatus.COMPENSATING || status == SagaStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
        }
    }
}
