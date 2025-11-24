package com.ecomm.events.order.kafka;

import com.ecomm.events.order.OrderCommunicationEvent;
import com.ecomm.events.order.OrderPlacedEvent;
import com.ecomm.events.order.domain.RecommendedProductSummary;
import com.ecomm.events.order.domain.Order;
import com.ecomm.events.order.domain.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_PLACED_TOPIC = "order.placed.v1";
    private static final String USER_EMAIL_OUTBOX_TOPIC = "user.email.outbox.v1";

    private String eventId() {
        return UUID.randomUUID().toString();
    }

    public void sendOrderPlacedEvent(Order order, List<RecommendedProductSummary> recs) {

        // 1) Publish ORDER_PLACED event
        OrderPlacedEvent placedEvent = OrderPlacedEvent.builder()
                .eventId(eventId())
                .orderId(order.getId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .totalAmount(order.getTotalAmount())
                .createdAt(Instant.now())
                .items(order.getItems().stream()
                        .map(this::toPayload)
                        .collect(Collectors.toList()))
                .build();

        kafkaTemplate.send(ORDER_PLACED_TOPIC, order.getId().toString(), placedEvent);
        log.info("EVENT → ORDER_PLACED {}", placedEvent);

        // 2) Publish Email Communication Event
        OrderCommunicationEvent commEvent = OrderCommunicationEvent.builder()
                .eventId(eventId())
                .orderId(order.getId())
                .userId(order.getUserId())
                .toEmail(order.getUserEmail())
                .subject("Your order #" + order.getId() + " is confirmed!")
                .templateName("order-confirmation")
                .orderSummary(OrderCommunicationEvent.OrderSummary.builder()
                        .totalAmount(order.getTotalAmount())
                        .itemNames(order.getItems().stream()
                                .map(OrderItem::getProductName)
                                .collect(Collectors.toList()))
                        .build())
                .recommendations(recs)
                .build();

        kafkaTemplate.send(USER_EMAIL_OUTBOX_TOPIC, order.getUserId().toString(), commEvent);
        log.info("EVENT → USER_EMAIL_OUTBOX {}", commEvent);
    }

    private OrderPlacedEvent.OrderItemPayload toPayload(OrderItem item) {
        return OrderPlacedEvent.OrderItemPayload.builder()
                .productId(Long.valueOf(item.getProductId()))
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .build();
    }
}
