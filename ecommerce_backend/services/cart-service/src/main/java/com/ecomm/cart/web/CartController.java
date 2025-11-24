package com.ecomm.cart.web;

import com.ecomm.cart.dto.AddCartItemRequest;
import com.ecomm.cart.dto.CartResponse;
import com.ecomm.cart.dto.UpdateCartItemRequest;
import com.ecomm.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "User shopping cart APIs")
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/health")
    public String health() {
        return "OK - cart-service";
    }

    @Operation(summary = "Get current user's cart")
    @GetMapping("/me")
    public CartResponse getMyCart() {
        String userId = currentUserId();
        return cartService.getMyCart(userId);
    }

    @Operation(summary = "Add an item to the current user's cart")
    @PostMapping("/items")
    public CartResponse addItem(@Valid @RequestBody AddCartItemRequest request) {
        String userId = currentUserId();
        return cartService.addItem(userId, request);
    }

    @Operation(summary = "Update quantity for an item in the cart")
    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(@PathVariable Long itemId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        String userId = currentUserId();
        return cartService.updateItem(userId, itemId, request);
    }

    @Operation(summary = "Remove an item from the cart")
    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@PathVariable Long itemId) {
        String userId = currentUserId();
        return cartService.removeItem(userId, itemId);
    }

    @Operation(summary = "Clear the current user's cart")
    @DeleteMapping
    public void clear() {
        String userId = currentUserId();
        cartService.clear(userId);
    }

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("User authentication required");
        }
        return auth.getName(); // JWT subject = userId
    }
}
