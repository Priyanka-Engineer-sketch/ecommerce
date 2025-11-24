package com.ecomm.ordersaga.messaging;

import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.events.order.SagaReplyEvent;
import com.ecomm.ordersaga.service.OrderSagaServiceKafka;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderSagaListeners {

    @Autowired
    private final OrderSagaServiceKafka sagaService;

    public OrderSagaListeners(OrderSagaServiceKafka sagaService) {
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
