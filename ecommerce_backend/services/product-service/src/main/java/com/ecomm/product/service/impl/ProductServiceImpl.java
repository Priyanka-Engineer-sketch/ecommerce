package com.ecomm.product.service.impl;

import com.ecomm.events.product.ProductStatus;
import com.ecomm.product.ai.ImageGenerationClient;
import com.ecomm.product.domain.Product;
import com.ecomm.product.dto.PageResponse;
import com.ecomm.product.dto.ProductRequest;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.repository.ProductRepository;
import com.ecomm.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ImageGenerationClient imageGenerationClient;

    @Override
    public ProductResponse create(ProductRequest request) {
        productRepository.findBySku(request.getSku()).ifPresent(p -> {
            throw new IllegalArgumentException("Product with SKU already exists: " + request.getSku());
        });

        String thumbnail = request.getThumbnailUrl();
        if (thumbnail == null || thumbnail.isBlank()) {
            thumbnail = imageGenerationClient.generateThumbnail(
                    request.getName(),
                    request.getDescription()
            );
        }

        Product product = Product.builder()
                .sku(request.getSku())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .brand(request.getBrand())
                .status(ProductStatus.ACTIVE)
                .thumbnailUrl(thumbnail)
                .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product existing = getEntity(id);

        if (!existing.getSku().equals(request.getSku())) {
            productRepository.findBySku(request.getSku()).ifPresent(p -> {
                throw new IllegalArgumentException("Product with SKU already exists: " + request.getSku());
            });
            existing.setSku(request.getSku());
        }

        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setStockQuantity(request.getStockQuantity());
        existing.setCategory(request.getCategory());
        existing.setBrand(request.getBrand());


//        existing.setThumbnailUrl(request.getThumbnailUrl());
        // If consumer doesn’t send thumbnail, regenerate using AI
        if (request.getThumbnailUrl() == null || request.getThumbnailUrl().isBlank()) {
            existing.setThumbnailUrl(
                    imageGenerationClient.generateThumbnail(
                            request.getName(),
                            request.getDescription()
                    )
            );
        } else {
            existing.setThumbnailUrl(request.getThumbnailUrl());
        }

        Product saved = productRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with sku: " + sku));
        return toResponse(product);
    }

    @Override
    public void delete(Long id) {
        Product existing = getEntity(id);
        productRepository.delete(existing);
    }

    @Override
    public ProductResponse updateStatus(Long id, ProductStatus status) {
        Product existing = getEntity(id);
        existing.setStatus(status);
        Product saved = productRepository.save(existing);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(
            String keyword,
            String category,
            Double minPrice,
            Double maxPrice,
            ProductStatus status,
            Pageable pageable
    ) {
        Page<Product> page;

        if (category != null && !category.isBlank()) {
            page = productRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            page = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else if (minPrice != null && maxPrice != null) {
            page = productRepository.findByPriceBetween(
                    minPrice,
                    maxPrice,
                    pageable
            );
        } else if (status != null) {
            page = productRepository.findByStatus(status, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }

        return toPageResponse(page, this::toResponse);
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
    }

    private ProductResponse toResponse(Product entity) {
        return ProductResponse.builder()
                .id(entity.getId())
                .sku(entity.getSku())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stockQuantity(entity.getStockQuantity())
                .category(entity.getCategory())
                .brand(entity.getBrand())
                .status(entity.getStatus())
                .thumbnailUrl(entity.getThumbnailUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private <T, R> PageResponse<R> toPageResponse(Page<T> page, Function<T, R> mapper) {
        return PageResponse.<R>builder()
                .content(page.map(mapper).getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
