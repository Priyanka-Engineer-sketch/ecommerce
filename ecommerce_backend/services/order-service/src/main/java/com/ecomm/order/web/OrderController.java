package com.ecomm.order.web;

import com.ecomm.order.domain.OrderStatus;
import com.ecomm.order.dto.CreateOrderRequest;
import com.ecomm.order.dto.OrderResponse;
import com.ecomm.order.dto.UpdateOrderRequest;
import com.ecomm.order.dto.UpdateOrderStatusRequest;
import com.ecomm.order.service.OrderApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;

@RestController
@RequestMapping("/orders")   // ✅ internal path; gateway will add /api and strip it
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    private static final EnumSet<OrderStatus> ALLOWED_STATUS_UPDATES =
            EnumSet.of(
                    OrderStatus.PENDING,
                    OrderStatus.CONFIRMED,
                    OrderStatus.READY_TO_SHIP,
                    OrderStatus.SHIPPED,
                    OrderStatus.DELIVERED,
                    OrderStatus.CANCELLED
            );

    // -------------------------------------------------------------------------
    // CREATE ORDER  (USER)
    // -------------------------------------------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {

        String userId = getCurrentUserId(); // from JWT (subject)

        return orderApplicationService.createOrder(userId, request);
    }

    // -------------------------------------------------------------------------
    // GET ONE ORDER
    // -------------------------------------------------------------------------
    @GetMapping("/{externalOrderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse getOrder(@PathVariable String externalOrderId) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        OrderResponse response = orderApplicationService.getOrderById(externalOrderId);

        // Non-admin can only see their own order
        if (!isAdmin && response.customer() != null) {
            String ownerId = response.customer().id();
            if (!currentUserId.equals(ownerId)) {
                throw new AccessDeniedException("You cannot access another user's order.");
            }
        }

        return response;
    }

    // -------------------------------------------------------------------------
    // LIST ORDERS
    // -------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<OrderResponse> listOrders(
            @RequestParam(name = "customerId", required = false) String customerIdFilter,
            @RequestParam(name = "status", required = false) OrderStatus statusFilter) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        return orderApplicationService.getOrders(
                currentUserId,
                isAdmin,
                customerIdFilter,
                statusFilter
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE ORDER (items / address, etc.)
    // -------------------------------------------------------------------------
    @PutMapping("/{externalOrderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse updateOrder(@PathVariable String externalOrderId,
                                     @Valid @RequestBody UpdateOrderRequest request) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        return orderApplicationService.updateOrder(
                externalOrderId,
                currentUserId,
                isAdmin,
                request
        );
    }

    // -------------------------------------------------------------------------
    // UPDATE ORDER STATUS
    // -------------------------------------------------------------------------
    @PatchMapping("/{externalOrderId}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse updateOrderStatus(@PathVariable String externalOrderId,
                                           @Valid @RequestBody UpdateOrderStatusRequest request) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        if (!ALLOWED_STATUS_UPDATES.contains(request.status())) {
            throw new IllegalArgumentException("Unsupported status: " + request.status());
        }

        // USER is NOT allowed to directly change status except cancellation
        if (!isAdmin && request.status() != OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Users can only cancel their order.");
        }

        return orderApplicationService.updateOrderStatus(
                externalOrderId,
                currentUserId,
                isAdmin,
                request.status()
        );
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new AccessDeniedException("User authentication required.");
        }
        // subject from JWT = userId (or email, depending on your JwtAuthFilter)
        return auth.getName();
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
