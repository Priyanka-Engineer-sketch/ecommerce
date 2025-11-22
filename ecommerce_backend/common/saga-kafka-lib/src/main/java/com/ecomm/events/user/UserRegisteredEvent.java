package com.ecomm.events.user;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String username,
        long occurredAtMs
) {}
