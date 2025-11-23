package com.ecomm.events.user;

public record UserLogoutAllEvent(
        String eventId,
        Long userId,
        long timestamp
) {}
