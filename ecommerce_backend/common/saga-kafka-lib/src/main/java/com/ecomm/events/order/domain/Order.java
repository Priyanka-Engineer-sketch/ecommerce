package com.ecomm.events.order.domain;

import com.ecomm.events.payment.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * External Reference (e.g., OR-2025-00001)
     */
    @Column(name = "external_order_id", unique = true, nullable = false, length = 100)
    private String externalOrderId;

    /**
     * Embedded Customer details (customerId, name, email)
     */
    @Embedded
    private CustomerInfo customer;

    /**
     * Embedded shipping address block
     */
    @Embedded
    private ShippingAddress shippingAddress;

    /**
     * One-to-many items
     */
    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Total amount of the order
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private double totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private OrderStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    private PaymentMethod  paymentMethod;


    // ---------------------------
    // Helper Methods
    // ---------------------------
    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
    }

    // ---------------------------
    // Derived Convenience Getters
    // ---------------------------
    public Long getUserId() {
        return customer != null ? Long.valueOf(customer.getCustomerId()) : null;
    }

    public String getUserEmail() {
        return customer != null ? customer.getEmail() : null;
    }

    // ---------------------------
    // Equals / HashCode for JPA
    // ---------------------------
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order that = (Order) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
