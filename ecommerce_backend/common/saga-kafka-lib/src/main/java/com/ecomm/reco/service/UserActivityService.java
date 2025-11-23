package com.ecomm.reco.service;

public interface UserActivityService {

    void registerLogin(Long userId, String ip, String userAgent, long timestamp);

    // in future:
    // void registerProductView(Long userId, Long productId);
    // void registerAddToCart(Long userId, Long productId);
}
