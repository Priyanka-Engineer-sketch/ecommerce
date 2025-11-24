package com.ecomm.events.order.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecommendedProductSummary {
    private Long id;
    private String sku;
    private String name;
    private BigDecimal price;
    private String category;
    private String brand;
}
