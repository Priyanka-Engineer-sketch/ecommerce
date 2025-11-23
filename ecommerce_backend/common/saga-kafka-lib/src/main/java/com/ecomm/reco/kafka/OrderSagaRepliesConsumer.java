package com.ecomm.reco.kafka;

import com.ecomm.events.order.OrderSagaReply;
import com.ecomm.events.saga.SagaStatus;
import com.ecomm.events.order.SagaStep;
import com.ecomm.reco.service.OrderActivityService;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSagaRepliesConsumer {

    private final OrderActivityService orderActivityService;

    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_REPLIES,
            groupId = "recommender-orders"
    )
    public void onSagaReply(@Payload OrderSagaReply reply) {

        log.info("Recommender saw saga reply: {}", reply);

        // We only care when saga status reaches COMPLETED:
        if (reply.status() == SagaStatus.COMPLETED
                && reply.step() == SagaStep.SHIPPING) {

            orderActivityService.orderCompleted(reply.orderId());
        }
    }
}
