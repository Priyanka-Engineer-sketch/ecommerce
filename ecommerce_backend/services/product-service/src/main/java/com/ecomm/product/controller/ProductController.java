package com.ecomm.product.controller;

import com.ecomm.events.product.ProductStatus;
import com.ecomm.product.dto.PageResponse;
import com.ecomm.product.dto.ProductRequest;
import com.ecomm.product.dto.ProductResponse;
import com.ecomm.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Products", description = "Manage products and search catalog")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(
            summary = "Create a product",
            description = "Creates a new product. Only ADMIN or SELLER can call this.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Product created",
                            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden")
            }
    )
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @Operation(
            summary = "Update a product",
            description = "Updates product data by ID. Only ADMIN or SELLER.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Updated product"),
                    @ApiResponse(responseCode = "404", description = "Product not found")
            }
    )
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ProductResponse update(@PathVariable Long id,
                                  @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @Operation(summary = "Update product status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public ProductResponse updateStatus(
            @PathVariable Long id,
            @RequestParam ProductStatus status
    ) {
        return productService.updateStatus(id, status);
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @Operation(summary = "Get product by SKU")
    @GetMapping("/sku/{sku}")
    public ProductResponse getBySku(@PathVariable String sku) {
        return productService.getBySku(sku);
    }

    @Operation(
            summary = "Search products",
            description = "Search by keyword, category, price range and status with pagination."
    )
    @GetMapping
    public PageResponse<ProductResponse> search(
            @Parameter(description = "Search keyword applied to product name")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Filter by category (case-insensitive)")
            @RequestParam(required = false) String category,
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) Double minPrice,
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) Double maxPrice,
            @Parameter(description = "Filter by product status")
            @RequestParam(required = false) ProductStatus status,
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field and direction, e.g. createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] parts = sort.split(",");
        Sort.Direction direction =
                (parts.length > 1 && "asc".equalsIgnoreCase(parts[1]))
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, parts[0]));
        return productService.search(keyword, category, minPrice, maxPrice, status, pageable);
    }

    @Operation(summary = "Delete product")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SELLER')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
