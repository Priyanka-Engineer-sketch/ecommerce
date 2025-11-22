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
@RequestMapping("/orders")    // IMPORTANT: /api prefix is handled by GATEWAY
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderApplicationService;

    // ------------------------------------------------------------------------
    // CONSTANTS FOR ORDER STATUS RULES
    // ------------------------------------------------------------------------
    private static final EnumSet<OrderStatus> USER_CANNOT_EDIT =
            EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.READY_TO_SHIP,
                    OrderStatus.SHIPPED, OrderStatus.DELIVERED, OrderStatus.CANCELLED);

    // ------------------------------------------------------------------------
// CREATE ORDER (USER)
// ------------------------------------------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {

        // Extract current logged-in user ID from SecurityContext (JWT)
        String userId = getCurrentUserId();

        // **Always override the customerId from JWT not from request**
        return orderApplicationService.createOrder(userId, request);
    }


    // ------------------------------------------------------------------------
    // GET ONE ORDER (USER can only see own order, ADMIN sees all)
    // ------------------------------------------------------------------------
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse getOrder(@PathVariable String orderId) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        OrderResponse response = orderApplicationService.getOrderById(orderId);

        if (!isAdmin && !response.customer().id().equals(currentUserId)) {
            throw new AccessDeniedException("You cannot access another user's order.");
        }

        return response;
    }

    // ------------------------------------------------------------------------
    // GET USER ORDERS / ADMIN ORDER SEARCH
    // ------------------------------------------------------------------------
    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<OrderResponse> getOrders(
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) OrderStatus status
    ) {
        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        return orderApplicationService.getOrders(currentUserId, isAdmin, customerId, status);
    }

    // ------------------------------------------------------------------------
    // UPDATE ORDER DETAILS (USER limited, ADMIN full)
    // ------------------------------------------------------------------------
    @PutMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse updateOrder(@PathVariable String orderId,
                                     @Valid @RequestBody UpdateOrderRequest request) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        OrderResponse order = orderApplicationService.getOrderById(orderId);

        boolean isOwner = order.customer().id().equals(currentUserId);

        // ---- USER BLOCKED LOGIC ----
        if (!isAdmin) {

            if (!isOwner) {
                throw new AccessDeniedException("You cannot modify another user's order.");
            }

            if (USER_CANNOT_EDIT.contains(order.status())) {
                throw new AccessDeniedException(
                        "Order cannot be modified by the user after it is " + order.status()
                );
            }
        }

        // ---- ADMIN SPECIAL RULE ----
        if (isAdmin && order.status() == OrderStatus.DELIVERED) {
            throw new AccessDeniedException("Delivered orders cannot be modified.");
        }

        return orderApplicationService.updateOrder(orderId, currentUserId, isAdmin, request);
    }

    // ------------------------------------------------------------------------
    // UPDATE ORDER STATUS (ADMIN FULL, USER very limited)
    // ------------------------------------------------------------------------
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public OrderResponse updateOrderStatus(@PathVariable String orderId,
                                           @Valid @RequestBody UpdateOrderStatusRequest request) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        // USER is NOT allowed to directly change status except cancellation
        if (!isAdmin && request.status() != OrderStatus.CANCELLED) {
            throw new AccessDeniedException("Users can only cancel their order.");
        }

        return orderApplicationService.updateOrderStatus(orderId, currentUserId, isAdmin, request.status());
    }

    // ------------------------------------------------------------------------
    // DELETE / CANCEL ORDER (USER cancels only own + only before shipping)
    // ------------------------------------------------------------------------
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public void deleteOrder(@PathVariable String orderId) {

        String currentUserId = getCurrentUserId();
        boolean isAdmin = isCurrentUserAdmin();

        OrderResponse order = orderApplicationService.getOrderById(orderId);

        boolean isOwner = order.customer().id().equals(currentUserId);

        if (!isAdmin) {
            if (!isOwner) {
                throw new AccessDeniedException("You cannot cancel another user's order.");
            }

            if (USER_CANNOT_EDIT.contains(order.status())) {
                throw new AccessDeniedException(
                        "Order cannot be cancelled after it is " + order.status()
                );
            }
        }

        orderApplicationService.deleteOrder(orderId, currentUserId, isAdmin);
    }

    // ------------------------------------------------------------------------
    // Helper Methods
    // ------------------------------------------------------------------------
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new AccessDeniedException("User authentication required.");
        return auth.getName();
    }

    private boolean isCurrentUserAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
