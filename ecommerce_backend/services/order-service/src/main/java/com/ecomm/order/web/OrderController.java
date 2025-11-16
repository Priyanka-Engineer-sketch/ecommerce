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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    // POST /api/orders – Create new order
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderApplicationService.createOrder(request);
    }

    // GET /api/orders/{orderId} – Fetch order by ID
    @GetMapping("/{orderId}")
    public OrderResponse getOrder(@PathVariable String orderId) {
        OrderResponse response = orderApplicationService.getOrderById(orderId);

        // Simulate current user context (replace later with real auth)
        String currentUserId = getCurrentUserId();       // from JWT/security
        boolean isAdmin = isCurrentUserAdmin();          // from JWT/security

        if (!isAdmin && !response.customer().id().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to access this order.");
        }

        return response;
    }

    // ---- Helpers to be replaced with real security context ----
    private String getCurrentUserId() {
        // TODO: Replace with real JWT principal or SecurityContext
        return "user-123";  // mock user ID
    }

    private boolean isCurrentUserAdmin() {
        // TODO: Replace with real role check from JWT
        return false;
    }

    @GetMapping
    public List<OrderResponse> getOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) OrderStatus status
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        return orderApplicationService.getOrders(currentUserId, isAdmin, customerId, status);
    }

    @PutMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable String orderId,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        return orderApplicationService.updateOrderStatus(orderId, currentUserId, isAdmin, request.status());
    }

    @PutMapping("/{orderId}")
    public OrderResponse updateOrder(@PathVariable String orderId,
                                     @Valid @RequestBody UpdateOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        return orderApplicationService.updateOrder(orderId, currentUserId, isAdmin, request);
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable String orderId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"));

        orderApplicationService.deleteOrder(orderId, isAdmin);
    }

}
