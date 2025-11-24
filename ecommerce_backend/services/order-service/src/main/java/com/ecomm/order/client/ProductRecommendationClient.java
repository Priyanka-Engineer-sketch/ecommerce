package com.ecomm.order.client;

import com.ecomm.events.order.domain.RecommendedProductSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductRecommendationClient {

    private final WebClient.Builder webClientBuilder;

    public List<RecommendedProductSummary> recommendForProduct(Long productId, Long userId, int limit) {
        try {
            WebClient client = webClientBuilder.baseUrl("http://product-service").build();

            Map<String, Object> body = client.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/recommendations/by-product/{productId}")
                            .queryParam("userId", userId)
                            .queryParam("limit", limit)
                            .build(productId))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (body == null || !body.containsKey("recommendations")) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> recs = (List<Map<String, Object>>) body.get("recommendations");

            return recs.stream()
                    .map(m -> RecommendedProductSummary.builder()
                            .id(((Number) m.get("id")).longValue())
                            .sku((String) m.get("sku"))
                            .name((String) m.get("name"))
                            .price(m.get("price") == null ? null :
                                    new java.math.BigDecimal(m.get("price").toString()))
                            .category((String) m.get("category"))
                            .brand((String) m.get("brand"))
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            log.warn("Failed to fetch recommendations for product {} and user {}: {}", productId, userId, ex.getMessage());
            return Collections.emptyList();
        }
    }
}
