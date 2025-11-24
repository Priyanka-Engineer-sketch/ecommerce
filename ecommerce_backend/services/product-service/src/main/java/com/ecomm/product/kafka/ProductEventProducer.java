package com.ecomm.product.kafka;

import com.ecomm.events.product.ProductChangedEvent;
import com.ecomm.product.domain.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String PRODUCT_CHANGED_TOPIC = "product.changed.v1";

    private String eventId() {
        return UUID.randomUUID().toString();
    }

    public void sendProductCreated(Product product) {
        send(product, "CREATED");
    }

    public void sendProductUpdated(Product product) {
        send(product, "UPDATED");
    }

    private void send(Product product, String type) {
        ProductChangedEvent event = ProductChangedEvent.builder()
                .eventId(eventId())
                .productId(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .category(product.getCategory())
                .brand(product.getBrand())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .status(product.getStatus())
                .thumbnailUrl(product.getThumbnailUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .type(type)
                .build();

        kafkaTemplate.send(PRODUCT_CHANGED_TOPIC, product.getId().toString(), event);
        log.info("EVENT → PRODUCT_CHANGED [{}] {}", type, event);
    }
}
