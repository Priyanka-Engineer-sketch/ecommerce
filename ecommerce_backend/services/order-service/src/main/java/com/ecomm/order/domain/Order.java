package com.ecomm.order.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String externalOrderId;

    @Embedded
    private CustomerInfo customer;

    @Embedded
    private ShippingAddress shippingAddress;

    @OneToMany(mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private Instant createdAt;
    private Instant updatedAt;

    // helper
    public void addItem(OrderItem item) {
        this.items.add(item);
        item.setOrder(this);
    }
}
