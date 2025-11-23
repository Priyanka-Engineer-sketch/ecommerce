package com.ecomm.inventory.saga;

import com.ecomm.events.order.OrderSagaReply;
import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.events.order.SagaStep;
import com.ecomm.events.saga.SagaStatus;
import com.ecomm.inventory.service.InventoryService;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventorySagaHandler {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = SagaKafkaTopics.ORDER_SAGA_START,
            groupId = "inventory-service"
    )
    public void onOrderSagaStart(@Payload OrderSagaStartEvent event) {

        log.info("Inventory-Service received SAGA START: {}", event);

        boolean reserved = inventoryService.reserveForOrder(Long.valueOf(event.orderId()), event.items());

        SagaStatus status = reserved
                ? SagaStatus.INVENTORY_RESERVED
                : SagaStatus.FAILED;

        OrderSagaReply reply = new OrderSagaReply(
                event.sagaId(),
                Long.parseLong(event.orderId()),
                SagaStep.INVENTORY,
                status,
                reserved ? null : "OUT_OF_STOCK",
                reserved ? null : "Insufficient stock",
                System.currentTimeMillis()
        );

        String key = SagaMessageKeys.commandKey(event.sagaId(), SagaStep.INVENTORY);

        // Reply to orchestrator / order-service
        kafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_REPLIES, key, reply);

        // If success → forward to Payment step
        if (reserved) {
            kafkaTemplate.send(
                    SagaKafkaTopics.ORDER_SAGA_CMD_PAYMENT,
                    SagaMessageKeys.commandKey(event.sagaId(), SagaStep.PAYMENT),
                    reply
            );
        }
    }
}
