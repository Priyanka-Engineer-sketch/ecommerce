package com.ecomm.notification;

/**
 * Simple abstraction for sending push notifications to a user.
 * You can later plug FCM, Web Push, mobile push, etc.
 */
public interface PushNotificationService {

    /**
     * Send a message to a user (identified by userId).
     */
    void sendToUser(Long userId, String message);
}
