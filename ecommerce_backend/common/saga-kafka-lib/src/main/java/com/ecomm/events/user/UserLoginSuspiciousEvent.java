package com.ecomm.events.user;

public record UserLoginSuspiciousEvent(
        String eventId,
        Long userId,
        String email,
        String ip,
        String userAgent,
        int riskScore,
        long timestamp
) {}
