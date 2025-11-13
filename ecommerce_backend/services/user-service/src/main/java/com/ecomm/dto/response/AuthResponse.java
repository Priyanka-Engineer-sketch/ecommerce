package com.ecomm.dto.response;

import lombok.Builder;

import java.util.Set;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long   expiresIn,
        Long   userId,
        String email,
        String username,
        Set<String> roles,
        Set<String> permissions
) {}