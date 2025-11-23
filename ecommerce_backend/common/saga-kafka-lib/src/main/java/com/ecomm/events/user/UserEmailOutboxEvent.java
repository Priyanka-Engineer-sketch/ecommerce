package com.ecomm.events.user;

public record UserEmailOutboxEvent(
        String eventId,
        String to,
        String template,
        String payload,
        long timestamp
) {}
