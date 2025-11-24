package com.ecomm.product.dto;

import com.ecomm.events.product.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ProductResponse {

    private Long id;
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
}
