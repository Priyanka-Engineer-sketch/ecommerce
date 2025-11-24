package com.ecomm.order.service;

import com.ecomm.events.order.OrderSagaReply;
import com.ecomm.events.order.SagaStep;
import com.ecomm.events.saga.SagaStatus;
import com.ecomm.events.order.domain.Order;
import com.ecomm.events.order.domain.OrderStatus;
import com.ecomm.order.repository.OrderRepository;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderSagaService {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_REPLIES,
            groupId = "order-service-saga"
    )
    public void onSagaReply(@Payload OrderSagaReply reply) {
        log.info("Order-service received saga reply: {}", reply);

        Order order = orderRepository.findById(reply.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for saga reply, id=" + reply.orderId()
                ));

        // Map saga status → our OrderStatus
        applyStatusTransition(order, reply);

        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
    }

    private void applyStatusTransition(Order order, OrderSagaReply reply) {

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
