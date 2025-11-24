package com.ecomm.product.domain;

import com.ecomm.events.product.ProductStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, scale = 2, precision = 15)
    private Double price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Size(max = 500)
    private String thumbnailUrl;
}
