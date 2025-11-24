package com.ecomm.product.service.impl;

import com.ecomm.events.product.ProductStatus;
import com.ecomm.product.domain.Product;
import com.ecomm.product.dto.RecommendationResult;
import com.ecomm.product.dto.RecommendedProductResponse;
import com.ecomm.product.repository.ProductRepository;
import com.ecomm.product.service.RecommendationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final ProductRepository productRepository;

    @Override
    public RecommendationResult recommendForProduct(Long productId, Long userId, int limit) {

        Product base = productRepository.findById(productId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Product not found: " + productId)
                );

        List<Product> candidates = new ArrayList<>();

        if (base.getCategory() != null && !base.getCategory().isBlank()) {
            Page<Product> byCategory = productRepository.findByCategoryIgnoreCaseAndIdNot(
                    base.getCategory(),
                    base.getId(),
                    PageRequest.of(0, limit)
            );
            candidates.addAll(byCategory.getContent());
        }

        if (candidates.size() < limit &&
                base.getBrand() != null &&
                !base.getBrand().isBlank()) {

            Page<Product> byBrand = productRepository.findByBrandIgnoreCaseAndIdNot(
                    base.getBrand(),
                    base.getId(),
                    PageRequest.of(0, limit)
            );

            byBrand.getContent().stream()
                    .filter(p -> candidates.stream().noneMatch(c -> c.getId().equals(p.getId())))
                    .limit(limit - candidates.size())
                    .forEach(candidates::add);
        }

        if (candidates.size() < limit) {
            Page<Product> latest = productRepository.findByStatusOrderByCreatedAtDesc(
                    ProductStatus.ACTIVE,
                    PageRequest.of(0, limit)
            );

            latest.getContent().stream()
                    .filter(p -> !p.getId().equals(base.getId()))
                    .filter(p -> candidates.stream().noneMatch(c -> c.getId().equals(p.getId())))
                    .limit(limit - candidates.size())
                    .forEach(candidates::add);
        }

        List<RecommendedProductResponse> mapped = candidates.stream()
                .map(this::toRecommended)
                .toList();

        return RecommendationResult.builder()
                .baseProductId(productId)
                .userId(userId)   // FIXED
                .recommendations(mapped)
                .build();
    }

    @Override
    public RecommendationResult recommendForUser(Long userId, int limit) {

        Page<Product> page = productRepository.findByStatusOrderByCreatedAtDesc(
                ProductStatus.ACTIVE,
                PageRequest.of(0, limit)
        );

        List<RecommendedProductResponse> mapped = page.getContent()
                .stream()
                .map(this::toRecommended)
                .toList();

        return RecommendationResult.builder()
                .userId(userId)   // FIXED
                .recommendations(mapped)
                .build();
    }

    private RecommendedProductResponse toRecommended(Product p) {
        return RecommendedProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .price(p.getPrice())
                .category(p.getCategory())
                .brand(p.getBrand())
                .thumbnailUrl(p.getThumbnailUrl())
                .build();
    }
}
