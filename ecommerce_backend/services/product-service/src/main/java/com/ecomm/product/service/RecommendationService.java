package com.ecomm.product.service;

import com.ecomm.product.dto.RecommendationResult;

public interface RecommendationService {

    RecommendationResult recommendForProduct(Long productId, Long userId, int limit);

    RecommendationResult recommendForUser(Long userId, int limit);
}
