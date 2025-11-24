package com.ecomm.product.service;

import com.ecomm.events.product.ProductStatus;
import com.ecomm.product.dto.PageResponse;
import com.ecomm.product.dto.ProductRequest;
import com.ecomm.product.dto.ProductResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse update(Long id, ProductRequest request);

    ProductResponse getById(Long id);

    ProductResponse getBySku(String sku);

    void delete(Long id);

    ProductResponse updateStatus(Long id, ProductStatus status);

    PageResponse<ProductResponse> search(
            String keyword,
            String category,
            Double minPrice,
            Double maxPrice,
            ProductStatus status,
            Pageable pageable
    );
}
