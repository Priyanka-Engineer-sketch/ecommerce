package com.ecomm.dto.response;

import lombok.Builder;

import java.time.Instant;

@Builder
public record OtpAuditResponse(
        Long id,
        String email,
        String type,
        boolean consumed,
        Instant createdAt,
        Instant expiresAt
) {}

