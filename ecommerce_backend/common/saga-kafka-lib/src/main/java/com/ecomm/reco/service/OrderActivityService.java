package com.ecomm.reco.service;

/**
 * Records order-related events for recommendation engine.
 * You can later persist to DB and use it for ML training.
 */
public interface OrderActivityService {

    /**
     * Called when an order has fully completed (saga completed).
     */
    void orderCompleted(Long orderId);

    // In future you can add:
    // void orderCancelled(Long orderId);
    // void productPurchased(Long userId, Long productId);
}
