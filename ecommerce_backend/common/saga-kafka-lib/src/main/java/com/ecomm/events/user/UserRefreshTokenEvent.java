package com.ecomm.events.user;

public record UserRefreshTokenEvent(
        String eventId,
        Long userId,
        String email,
        long timestamp
) {}
