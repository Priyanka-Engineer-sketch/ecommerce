package com.ecomm.events.order;

import com.ecomm.events.order.domain.RecommendedProductSummary;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderCommunicationEvent {

    private String eventId;
    private Long orderId;
    private Long userId;
    private String toEmail;
    private String subject;
    private String templateName;     // e.g., "order-confirmation"
    private OrderSummary orderSummary;
    private List<RecommendedProductSummary> recommendations;

    @Data
    @Builder
    public static class OrderSummary {
        private double totalAmount;
        private List<String> itemNames;
    }
}
