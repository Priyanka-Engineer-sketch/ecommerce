package com.ecomm.events.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductChangedEvent {

    private String eventId;

    private Long productId;
    private String sku;
    private String name;
    private String description;

    private Double price;
    private Integer stockQuantity;
    private String category;
    private String brand;

    private ProductStatus status;

    private String thumbnailUrl;

    private Instant createdAt;
    private Instant updatedAt;

    /**
     * CREATED or UPDATED
     */
    private String type;
}
