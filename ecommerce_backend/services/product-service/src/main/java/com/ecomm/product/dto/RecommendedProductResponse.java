package com.ecomm.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecommendedProductResponse {
    private Long id;
    private String sku;
    private String name;
    private Double price;
    private String category;
    private String brand;
    private String thumbnailUrl;
}
