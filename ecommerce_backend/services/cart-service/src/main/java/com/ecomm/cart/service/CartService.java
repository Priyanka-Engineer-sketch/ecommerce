package com.ecomm.cart.service;

import com.ecomm.cart.dto.AddCartItemRequest;
import com.ecomm.cart.dto.CartResponse;
import com.ecomm.cart.dto.UpdateCartItemRequest;

public interface CartService {

    CartResponse getMyCart(String userId);

    CartResponse addItem(String userId, AddCartItemRequest request);

    CartResponse updateItem(String userId, Long itemId, UpdateCartItemRequest request);

    CartResponse removeItem(String userId, Long itemId);

    void clear(String userId);
}
