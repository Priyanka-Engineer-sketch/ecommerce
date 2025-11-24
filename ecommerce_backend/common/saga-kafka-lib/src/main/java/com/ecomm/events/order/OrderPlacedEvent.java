package com.ecomm.events.order;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderPlacedEvent {

    private String eventId;
    private Long orderId;
    private Long userId;
    private String userEmail;
    private double totalAmount;
    private Instant createdAt;

    private List<OrderItemPayload> items;

    @Data
    @Builder
    public static class OrderItemPayload {
        private Long productId;
        private String productName;
        private Integer quantity;
        private double price;
    }
}
