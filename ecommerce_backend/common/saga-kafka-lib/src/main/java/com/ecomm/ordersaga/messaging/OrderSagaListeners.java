package com.ecomm.ordersaga.messaging;

import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.events.order.SagaReplyEvent;
import com.ecomm.ordersaga.service.OrderSagaService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaListeners {

    private final OrderSagaService sagaService;

    public OrderSagaListeners(OrderSagaService sagaService) {
        this.sagaService = sagaService;
    }

    @KafkaListener(
            topics = "#{T(com.ecomm.saga.kafka.SagaKafkaTopics).ORDER_SAGA_START}",
            groupId = "order-saga-orchestrator"
    )
    public void onOrderSagaStart(OrderSagaStartEvent event) {
        sagaService.startSaga(event);
    }

    @KafkaListener(
            topics = "#{T(com.ecomm.saga.kafka.SagaKafkaTopics).ORDER_SAGA_REPLIES}",
            groupId = "order-saga-orchestrator"
    )
    public void onSagaReply(SagaReplyEvent reply) {
        sagaService.handleReply(reply);
    }
}
