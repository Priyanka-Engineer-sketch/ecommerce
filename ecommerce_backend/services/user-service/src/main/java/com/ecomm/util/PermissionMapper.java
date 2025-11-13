package com.ecomm.util;

import com.ecomm.dto.response.PermissionResponse;
import com.ecomm.entity.Permission;

public final class PermissionMapper {
    private PermissionMapper() {}

    public static PermissionResponse toResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .build();
    }
}
