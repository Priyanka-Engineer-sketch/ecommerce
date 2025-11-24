package com.ecomm.shipping.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingSagaListener {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SHIPPING_COMMAND_TOPIC = "order.saga.commands.shipping";
    private static final String ORDER_SAGA_REPLIES_TOPIC = "order.saga.replies";

    @KafkaListener(topics = SHIPPING_COMMAND_TOPIC, groupId = "shipping-service")
    public void handleShippingCommand(Map<String, Object> payload) {
        log.info("Received shipping saga command: {}", payload);

        // TODO: parse payload and call real shipping logic here

        // For now, just log and pretend success
        log.info("Shipping saga: SUCCESS (dummy implementation)");

        // TODO: build and send real reply event (OrderSagaReplyEvent) back to ORDER_SAGA_REPLIES_TOPIC
    }
}
