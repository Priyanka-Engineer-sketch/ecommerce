package com.ecomm.events.user;

// generic email outbox event if you later want to send via a dedicated email service
public record UserEmailOutboxEvent(
        String to,
        String template,
        String payloadJson,
        long occurredAtMs
) {}
