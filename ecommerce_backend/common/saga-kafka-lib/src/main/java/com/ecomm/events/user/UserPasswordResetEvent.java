package com.ecomm.events.user;

public record UserPasswordResetEvent(
        String eventId,
        Long userId,
        String email,
        long occurredAtMs
) {}
