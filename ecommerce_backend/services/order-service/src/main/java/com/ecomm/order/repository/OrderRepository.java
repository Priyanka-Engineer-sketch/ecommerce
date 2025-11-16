package com.ecomm.order.repository;

import com.ecomm.order.domain.Order;
import com.ecomm.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByExternalOrderId(String externalOrderId);
    List<Order> findByCustomer_CustomerId(String customerId);
    List<Order> findByCustomer_CustomerIdAndStatus(String customerId, OrderStatus status);
    List<Order> findByStatus(OrderStatus status);
}
