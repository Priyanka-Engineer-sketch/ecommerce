package com.ecomm.order.service;

import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.events.order.SagaStep;
import com.ecomm.order.dto.UpdateOrderRequest;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import com.ecomm.order.domain.*;
import com.ecomm.order.dto.CreateOrderRequest;
import com.ecomm.order.dto.OrderResponse;
import com.ecomm.order.repository.OrderRepository;
import com.ecomm.order.saga.OrderSagaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> sagaKafkaTemplate;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. Build Order entity
        CustomerInfo customer = CustomerInfo.builder()
                .customerId(request.customer().id())
                .name(request.customer().name())
                .email(request.customer().email())
                .build();

        List<OrderItem> items = request.items().stream()
                .map(i -> OrderItem.builder()
                        .productId(i.id())
                        .productName(i.name())
                        .price(i.price())
                        .quantity(i.quantity())
                        .build())
                .toList();

        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        Order order = Order.builder()
                .externalOrderId("O-" + System.currentTimeMillis())
                .customer(customer)
                .items(items)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        order = orderRepository.save(order);

        // 2. Create sagaId and event
        String sagaId = OrderSagaMapper.newSagaId();
        OrderSagaStartEvent event = OrderSagaMapper.toStartEvent(sagaId, order);

        // 3. Send event to Kafka
        String key = SagaMessageKeys.commandKey(sagaId, SagaStep.INVENTORY);
        sagaKafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_START, key, event);

        // 4. Return response
        return new OrderResponse(
                order.getExternalOrderId(),
                request.customer(),
                request.items(),
                total,
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String externalOrderId) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        return new OrderResponse(
                order.getExternalOrderId(),
                new CreateOrderRequest.CustomerDto(
                        order.getCustomer().getCustomerId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getEmail()
                ),
                order.getItems().stream()
                        .map(i -> new CreateOrderRequest.OrderItemDto(
                                i.getProductId(),
                                i.getProductName(),
                                i.getPrice(),
                                i.getQuantity()
                        ))
                        .toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String currentUserId, boolean isAdmin, String customerId, OrderStatus status) {
        List<Order> orders;

        if (isAdmin) {
            if (customerId != null && status != null) {
                orders = orderRepository.findByCustomer_CustomerIdAndStatus(customerId, status);
            } else if (customerId != null) {
                orders = orderRepository.findByCustomer_CustomerId(customerId);
            } else if (status != null) {
                orders = orderRepository.findByStatus(status);
            } else {
                orders = orderRepository.findAll();
            }
        } else {
            if (status != null) {
                orders = orderRepository.findByCustomer_CustomerIdAndStatus(currentUserId, status);
            } else {
                orders = orderRepository.findByCustomer_CustomerId(currentUserId);
            }
        }

        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        return new OrderResponse(
                order.getExternalOrderId(),
                new CreateOrderRequest.CustomerDto(
                        order.getCustomer().getCustomerId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getEmail()
                ),
                order.getItems().stream()
                        .map(i -> new CreateOrderRequest.OrderItemDto(
                                i.getProductId(),
                                i.getProductName(),
                                i.getPrice(),
                                i.getQuantity()
                        )).toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    @Transactional
    public OrderResponse updateOrderStatus(String externalOrderId, String currentUserId, boolean isAdmin, OrderStatus newStatus) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        boolean ownsOrder = order.getCustomer().getCustomerId().equals(currentUserId);

        if (!isAdmin && !(ownsOrder && newStatus == OrderStatus.CANCELLED)) {
            throw new AccessDeniedException("You are not allowed to update this order.");
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());

        return new OrderResponse(
                order.getExternalOrderId(),
                new CreateOrderRequest.CustomerDto(
                        order.getCustomer().getCustomerId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getEmail()
                ),
                order.getItems().stream()
                        .map(i -> new CreateOrderRequest.OrderItemDto(
                                i.getProductId(),
                                i.getProductName(),
                                i.getPrice(),
                                i.getQuantity()
                        )).toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    @Transactional
    public OrderResponse updateOrder(String externalOrderId, String currentUserId, boolean isAdmin, UpdateOrderRequest request) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        boolean ownsOrder = order.getCustomer().getCustomerId().equals(currentUserId);

        if (!isAdmin && !ownsOrder) {
            throw new AccessDeniedException("You are not allowed to update this order.");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be updated.");
        }

        List<OrderItem> updatedItems = request.items().stream()
                .map(i -> OrderItem.builder()
                        .productId(i.id())
                        .productName(i.name())
                        .price(i.price())
                        .quantity(i.quantity())
                        .build())
                .toList();

        order.setItems(updatedItems);
        double total = updatedItems.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
        order.setTotalAmount(total);
        order.setUpdatedAt(Instant.now());

        return new OrderResponse(
                order.getExternalOrderId(),
                new CreateOrderRequest.CustomerDto(
                        order.getCustomer().getCustomerId(),
                        order.getCustomer().getName(),
                        order.getCustomer().getEmail()
                ),
                request.items().stream()
                        .map(i -> new CreateOrderRequest.OrderItemDto(i.id(), i.name(), i.price(), i.quantity()))
                        .toList(),
                total,
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    @Transactional
    public void deleteOrder(String externalOrderId, boolean isAdmin) {
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can delete orders.");
        }

        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        orderRepository.delete(order);
    }


}
