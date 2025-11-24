package com.ecomm.cart.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private Long productId;

    @Column(length = 100)
    private String productName;

    private String sku;

    @Column(nullable = false)
    private Double price;   // snapshot price at time of adding

    @Column(nullable = false)
    private int quantity;
}
