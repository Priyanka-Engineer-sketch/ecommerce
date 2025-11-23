package com.ecomm.events.user;

public record UserRegisteredEvent(
        String eventId,
        Long userId,
        String email,
        String username,
        long timestamp
) {}
