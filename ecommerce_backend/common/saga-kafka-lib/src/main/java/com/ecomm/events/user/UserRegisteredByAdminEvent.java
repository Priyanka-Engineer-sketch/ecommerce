package com.ecomm.events.user;

public record UserRegisteredByAdminEvent(
        String eventId,
        Long userId,
        String email,
        String username,
        long timestamp
) {}
