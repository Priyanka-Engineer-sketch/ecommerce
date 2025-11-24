package com.ecomm.cart.service.impl;

import com.ecomm.cart.domain.Cart;
import com.ecomm.cart.domain.CartItem;
import com.ecomm.cart.dto.AddCartItemRequest;
import com.ecomm.cart.dto.CartItemResponse;
import com.ecomm.cart.dto.CartResponse;
import com.ecomm.cart.dto.UpdateCartItemRequest;
import com.ecomm.cart.repository.CartRepository;
import com.ecomm.cart.service.CartService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getMyCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));
        return mapToResponse(cart);
    }

    @Override
    public CartResponse addItem(String userId, AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createEmptyCart(userId));

        // if item already exists → update qty
        CartItem existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(request.productId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.quantity());
            existing.setPrice(request.price()); // keep latest price snapshot
        } else {
            CartItem item = CartItem.builder()
                    .productId(request.productId())
                    .productName(request.name())
                    .sku(request.sku())
                    .price(request.price())
                    .quantity(request.quantity())
                    .build();
            cart.addItem(item);
        }

        cart.setUpdatedAt(Instant.now());
        cart = cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    public CartResponse updateItem(String userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Item not found in cart: " + itemId));

        item.setQuantity(request.quantity());
        cart.setUpdatedAt(Instant.now());
        cart = cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    public CartResponse removeItem(String userId, Long itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user " + userId));

        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cart.setUpdatedAt(Instant.now());
        cart = cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    public void clear(String userId) {
        cartRepository.findByUserId(userId).ifPresent(cartRepository::delete);
    }

    // ---------------- helpers ----------------

    private Cart createEmptyCart(String userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return cartRepository.save(cart);
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(i -> new CartItemResponse(
                        i.getId(),
                        i.getProductId(),
                        i.getSku(),
                        i.getProductName(),
                        i.getPrice(),
                        i.getQuantity()
                ))
                .toList();

        int totalItems = items.stream().mapToInt(CartItemResponse::quantity).sum();
        double totalAmount = items.stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                items,
                totalItems,
                totalAmount
        );
    }
}
