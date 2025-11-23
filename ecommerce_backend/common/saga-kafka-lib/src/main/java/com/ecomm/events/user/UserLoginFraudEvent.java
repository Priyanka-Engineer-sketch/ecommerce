package com.ecomm.events.user;

public record UserLoginFraudEvent(
        String eventId,
        Long userId,
        String email,
        String ip,
        String userAgent,
        long timestamp
) {}
