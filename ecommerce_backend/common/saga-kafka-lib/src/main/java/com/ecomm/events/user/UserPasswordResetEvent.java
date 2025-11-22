package com.ecomm.events.user;

public record UserPasswordResetEvent(
        Long userId,
        String email,
        long occurredAtMs
) {}
