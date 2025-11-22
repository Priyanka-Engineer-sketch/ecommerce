package com.ecomm.order.service;

import com.ecomm.events.order.OrderSagaStartEvent;
import com.ecomm.events.order.SagaStep;
import com.ecomm.order.domain.*;
import com.ecomm.order.dto.CreateOrderRequest;
import com.ecomm.order.dto.OrderResponse;
import com.ecomm.order.dto.UpdateOrderRequest;
import com.ecomm.order.repository.OrderRepository;
import com.ecomm.order.saga.OrderSagaMapper;
import com.ecomm.saga.kafka.SagaKafkaTopics;
import com.ecomm.saga.kafka.SagaMessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> sagaKafkaTemplate;

    // statuses where USER cannot modify/cancel
    private static final EnumSet<OrderStatus> USER_CANNOT_EDIT =
            EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.READY_TO_SHIP,
                    OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    // -------------------------------------------------------------------------
    // CREATE ORDER
    // -------------------------------------------------------------------------
    @Transactional
    public OrderResponse createOrder(String userIdFromToken, CreateOrderRequest request) {

        // 1. Build CustomerInfo – ALWAYS override with userId from JWT
        CustomerInfo customer = CustomerInfo.builder()
                .customerId(userIdFromToken)
                .name(request.customer().name())
                .email(request.customer().email())
                .build();

        // 2. Build ShippingAddress
        ShippingAddress shipping = ShippingAddress.builder()
                .line1(request.shippingAddress().line1())
                .line2(request.shippingAddress().line2())
                .city(request.shippingAddress().city())
                .state(request.shippingAddress().state())
                .postalCode(request.shippingAddress().postalCode())
                .country(request.shippingAddress().country())
                .build();

        // 3. Build items
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

        // 4. Create Order
        Order order = Order.builder()
                .externalOrderId("O-" + System.currentTimeMillis())
                .customer(customer)
                .shippingAddress(shipping)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        items.forEach(order::addItem);

        order = orderRepository.save(order);

        // 5. Start Saga
        String sagaId = OrderSagaMapper.newSagaId();
        OrderSagaStartEvent event = OrderSagaMapper.toStartEvent(sagaId, order);
        String key = SagaMessageKeys.commandKey(sagaId, SagaStep.INVENTORY);
        sagaKafkaTemplate.send(SagaKafkaTopics.ORDER_SAGA_START, key, event);

        // 6. Return DTO
        return mapToOrderResponse(order);
    }

    // -------------------------------------------------------------------------
    // GET ORDER
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String externalOrderId) {
        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));
        return mapToOrderResponse(order);
    }

    // -------------------------------------------------------------------------
    // LIST ORDERS
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String currentUserId, boolean isAdmin,
                                         String customerIdFilter, OrderStatus statusFilter) {

        List<Order> orders;

        if (isAdmin) {
            // admin search
            if (customerIdFilter != null && statusFilter != null) {
                orders = orderRepository.findByCustomer_CustomerIdAndStatus(customerIdFilter, statusFilter);
            } else if (customerIdFilter != null) {
                orders = orderRepository.findByCustomer_CustomerId(customerIdFilter);
            } else if (statusFilter != null) {
                orders = orderRepository.findByStatus(statusFilter);
            } else {
                orders = orderRepository.findAll();
            }
        } else {
            // user: always forced to own id
            if (statusFilter != null) {
                orders = orderRepository.findByCustomer_CustomerIdAndStatus(currentUserId, statusFilter);
            } else {
                orders = orderRepository.findByCustomer_CustomerId(currentUserId);
            }
        }

        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    // -------------------------------------------------------------------------
    // UPDATE ORDER DETAILS
    // -------------------------------------------------------------------------
    @Transactional
    public OrderResponse updateOrder(String externalOrderId, String currentUserId,
                                     boolean isAdmin, UpdateOrderRequest request) {

        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        boolean ownsOrder = order.getCustomer().getCustomerId().equals(currentUserId);

        if (!isAdmin) {
            if (!ownsOrder) {
                throw new AccessDeniedException("You cannot modify another user's order.");
            }
            if (USER_CANNOT_EDIT.contains(order.getStatus())) {
                throw new AccessDeniedException("Order cannot be modified after it is " + order.getStatus());
            }
        } else {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Delivered orders cannot be modified.");
            }
        }

        // update shipping
        order.setShippingAddress(ShippingAddress.builder()
                .line1(request.shippingAddress().line1())
                .line2(request.shippingAddress().line2())
                .city(request.shippingAddress().city())
                .state(request.shippingAddress().state())
                .postalCode(request.shippingAddress().postalCode())
                .country(request.shippingAddress().country())
                .build());

        // update items
        order.getItems().clear();
        List<OrderItem> newItems = request.items().stream()
                .map(i -> OrderItem.builder()
                        .productId(i.id())
                        .productName(i.name())
                        .price(i.price())
                        .quantity(i.quantity())
                        .build())
                .toList();
        newItems.forEach(order::addItem);

        double newTotal = newItems.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        order.setTotalAmount(newTotal);
        order.setUpdatedAt(Instant.now());

        return mapToOrderResponse(order);
    }

    // -------------------------------------------------------------------------
    // UPDATE ORDER STATUS
    // -------------------------------------------------------------------------
    @Transactional
    public OrderResponse updateOrderStatus(String externalOrderId, String currentUserId,
                                           boolean isAdmin, OrderStatus newStatus) {

        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        boolean ownsOrder = order.getCustomer().getCustomerId().equals(currentUserId);

        if (!isAdmin) {
            if (!ownsOrder) {
                throw new AccessDeniedException("You cannot modify another user's order.");
            }
            // user can ONLY cancel, and only if current status is still modifiable
            if (newStatus != OrderStatus.CANCELLED) {
                throw new AccessDeniedException("Users can only cancel their own orders.");
            }
            if (USER_CANNOT_EDIT.contains(order.getStatus())) {
                throw new AccessDeniedException("Order cannot be cancelled after it is " + order.getStatus());
            }
        } else {
            // admin restrictions
            if (order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Delivered orders cannot be changed.");
            }
        }

        order.setStatus(newStatus);
        order.setUpdatedAt(Instant.now());

        // TODO: send ORDER_STATUS_UPDATED / CANCELLED events here if needed

        return mapToOrderResponse(order);
    }

    // -------------------------------------------------------------------------
    // DELETE / CANCEL ORDER
    // -------------------------------------------------------------------------
    @Transactional
    public void deleteOrder(String externalOrderId, String currentUserId, boolean isAdmin) {

        Order order = orderRepository.findByExternalOrderId(externalOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + externalOrderId));

        boolean ownsOrder = order.getCustomer().getCustomerId().equals(currentUserId);

        if (!isAdmin) {
            // For USER: treat DELETE as "cancel"
            if (!ownsOrder) {
                throw new AccessDeniedException("You cannot cancel another user's order.");
            }
            if (USER_CANNOT_EDIT.contains(order.getStatus())) {
                throw new AccessDeniedException("Order cannot be cancelled after it is " + order.getStatus());
            }

            order.setStatus(OrderStatus.CANCELLED);
            order.setUpdatedAt(Instant.now());
            // TODO: publish OrderCancelledEvent if you want
        } else {
            // ADMIN: hard delete allowed except DELIVERED
            if (order.getStatus() == OrderStatus.DELIVERED) {
                throw new IllegalStateException("Delivered orders cannot be deleted.");
            }
            orderRepository.delete(order);
        }
    }

    // -------------------------------------------------------------------------
    // MAPPER
    // -------------------------------------------------------------------------
    private OrderResponse mapToOrderResponse(Order order) {
        var customerDto = new CreateOrderRequest.CustomerDto(
                order.getCustomer().getCustomerId(),
                order.getCustomer().getName(),
                order.getCustomer().getEmail()
        );

        var shippingDto = new CreateOrderRequest.ShippingAddressDto(
                order.getShippingAddress().getLine1(),
                order.getShippingAddress().getLine2(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getState(),
                order.getShippingAddress().getPostalCode(),
                order.getShippingAddress().getCountry()
        );

        var itemDtos = order.getItems().stream()
                .map(i -> new CreateOrderRequest.OrderItemDto(
                        i.getProductId(),
                        i.getProductName(),
                        i.getPrice(),
                        i.getQuantity()
                ))
                .toList();

        return new OrderResponse(
                order.getExternalOrderId(),
                customerDto,
                shippingDto,
                itemDtos,
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
