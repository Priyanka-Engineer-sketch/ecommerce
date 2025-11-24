package com.ecomm.product.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecommendationResult {
    private Long baseProductId;
    private Long userId;
    private List<RecommendedProductResponse> recommendations;
}
