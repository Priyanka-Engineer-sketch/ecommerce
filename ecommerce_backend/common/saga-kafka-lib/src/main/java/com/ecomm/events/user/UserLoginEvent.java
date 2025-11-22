package com.ecomm.events.user;

public record UserLoginEvent(
        String eventId,
        Long userId,
        String email,
        String ip,
        String userAgent,
        int riskScore,
        long occurredAtMs,
        long timestamp
) {}
