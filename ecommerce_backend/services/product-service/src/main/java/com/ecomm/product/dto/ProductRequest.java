package com.ecomm.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank
    @Size(max = 100)
    private String sku;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private Double price;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @Size(max = 100)
    private String category;

    @Size(max = 100)
    private String brand;

    @Size(max = 500)
    private String thumbnailUrl;
}
