package com.ecomm.ordersaga.service;

import com.ecomm.events.order.*;
import com.ecomm.events.saga.SagaStatus;
import com.ecomm.ordersaga.domain.repository.OrderSagaRepository;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import com.ecomm.ordersaga.domain.OrderSagaEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderSagaServiceKafka {

    private final OrderSagaRepository sagaRepository;
    private final KafkaTemplate<String, Object> sagaKafkaTemplate;

    public OrderSagaServiceKafka(OrderSagaRepository sagaRepository,
                                 KafkaTemplate<String, Object> sagaKafkaTemplate) {
        this.sagaRepository = sagaRepository;
        this.sagaKafkaTemplate = sagaKafkaTemplate;
    }

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
    }

    @Transactional
    public void handleReply(SagaReplyEvent reply) {
        OrderSagaEntity saga = sagaRepository.findById(reply.sagaId())
                .orElseThrow(() -> new IllegalStateException("Saga not found: " + reply.sagaId()));

        if (!reply.success()) {
            saga.setStatus(SagaStatus.COMPENSATING);
            saga.setLastError(reply.errorMessage());
            sagaRepository.save(saga);
            // TODO: send compensation commands (reverse inventory / refund payment)
            return;
        }

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
                null,                // userId (you can pass from start event later)
                null                 // amount (can be event.totalAmount())
        );

        String key = SagaMessageKeys.commandKey(saga.getSagaId(), SagaStep.PAYMENT);

        sagaKafkaTemplate.send(
                SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT,
                key,
                cmd
        );
    }

    private void handlePaymentSuccess(OrderSagaEntity saga, SagaReplyEvent reply) {
        saga.setPaymentDone(true);
        saga.setStatus(SagaStatus.PAYMENT_AUTHORIZED);
        sagaRepository.save(saga);

        ShippingCommand cmd = new ShippingCommand(
                saga.getSagaId(),
                saga.getOrderId(),
                null  // userId (optional)
        );

        String key = SagaMessageKeys.commandKey(saga.getSagaId(), SagaStep.SHIPPING);

        sagaKafkaTemplate.send(
                SagaKafkaTopics.ORDER_SAGA_CMD_SHIPPING,
                key,
                cmd
        );
    }

    private void handleShippingSuccess(OrderSagaEntity saga, SagaReplyEvent reply) {
        saga.setShippingDone(true);
        saga.setStatus(SagaStatus.COMPLETED);
        sagaRepository.save(saga);

        // TODO (optional): send OrderSagaResultEvent to order-service
        // sagaKafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_RESULT, ..., ...);
    }
}
