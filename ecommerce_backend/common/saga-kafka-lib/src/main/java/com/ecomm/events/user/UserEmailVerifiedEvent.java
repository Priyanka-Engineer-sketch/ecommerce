package com.ecomm.events.user;

public record UserEmailVerifiedEvent(
        String eventId,
        Long userId,
        String email,
        long timestamp
) {}
