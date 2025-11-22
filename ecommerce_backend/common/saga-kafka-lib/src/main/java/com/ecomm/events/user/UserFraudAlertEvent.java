package com.ecomm.events.user;

public record UserFraudAlertEvent(
        Long userId,
        String email,
        int riskScore,
        String reason,
        long occurredAtMs
) {}
