package com.ecomm.order.repository;

import com.ecomm.order.domain.Order;
import com.ecomm.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Look up by external business ID (O-12345...)
    Optional<Order> findByExternalOrderId(String externalOrderId);

    // All orders for a customer
    List<Order> findByCustomer_CustomerId(String customerId);

    // Orders for a customer with specific status
    List<Order> findByCustomer_CustomerIdAndStatus(String customerId, OrderStatus status);

    // All orders with a specific status
    List<Order> findByStatus(OrderStatus status);
}
