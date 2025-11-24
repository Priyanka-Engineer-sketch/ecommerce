package com.ecomm.product.controller;

import com.ecomm.product.dto.RecommendationResult;
import com.ecomm.product.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Product Recommendations", description = "AI-like recommendation APIs")
@RestController
@RequestMapping("/products/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(
            summary = "Recommend products by base product",
            description = "Returns recommended products based on category, brand and popularity."
    )
    @GetMapping("/by-product/{productId}")
    public RecommendationResult recommendForProduct(
            @PathVariable Long productId,
            @RequestParam(required = false) String userId,
            @RequestParam(defaultValue = "6") int limit
    ) {
        return recommendationService.recommendForProduct(productId, Long.valueOf(userId), limit);
    }

    @Operation(
            summary = "Recommend products for user",
            description = "Returns a set of recommended products for a given user (homepage feed, etc.)."
    )
    @GetMapping("/for-user/{userId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','SELLER')")
    public RecommendationResult recommendForUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return recommendationService.recommendForUser(Long.valueOf(userId), limit);
    }
}
