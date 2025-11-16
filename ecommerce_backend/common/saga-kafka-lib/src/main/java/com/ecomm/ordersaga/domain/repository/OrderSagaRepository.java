package com.ecomm.ordersaga.domain.repository;

import com.ecomm.ordersaga.domain.OrderSagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaRepository extends JpaRepository<OrderSagaEntity, String> {
}
