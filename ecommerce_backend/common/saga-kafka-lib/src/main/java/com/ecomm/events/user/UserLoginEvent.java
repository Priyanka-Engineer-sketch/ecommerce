package com.ecomm.events.user;

public record UserLoginEvent(
        Long userId,
        String email,
        String ip,
        String userAgent,
        int riskScore,
        long occurredAtMs
) {}
