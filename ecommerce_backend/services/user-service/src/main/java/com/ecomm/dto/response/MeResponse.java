package com.ecomm.dto.response;

import lombok.Builder;
import java.util.Set;

@Builder
public record MeResponse(
        Long id,
        String username,
        String email,
        String phone,
        Boolean isActive,
        Set<String> roles,
        Set<String> permissions
) {}
