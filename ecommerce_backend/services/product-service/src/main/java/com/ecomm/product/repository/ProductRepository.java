package com.ecomm.product.repository;

import com.ecomm.events.product.ProductStatus;
import com.ecomm.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    // ✅ Use BigDecimal, not Double
    Page<Product> findByPriceBetween(Double min, Double max, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    // ✅ Used by RecommendationServiceImpl (no "Top10" + generic Pageable)
    Page<Product> findByCategoryIgnoreCaseAndIdNot(String category, Long excludeId, Pageable pageable);

    Page<Product> findByBrandIgnoreCaseAndIdNot(String brand, Long excludeId, Pageable pageable);

    // ✅ Single, clean version – RecommendationServiceImpl uses this
    Page<Product> findByStatusOrderByCreatedAtDesc(ProductStatus status, Pageable pageable);
}
