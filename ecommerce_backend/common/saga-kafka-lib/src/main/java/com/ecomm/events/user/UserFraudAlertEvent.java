package com.ecomm.events.user;

public record UserFraudAlertEvent(
        String eventId,
        Long userId,
        String email,
        int riskScore,
        String reason,
        long occurredAtMs
) {}
