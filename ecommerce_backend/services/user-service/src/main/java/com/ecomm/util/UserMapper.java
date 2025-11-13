package com.ecomm.util;

import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.Permission;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {
    public static UserResponse toResponse(User u) {
        if (u == null) return null;
        Set<String> roles = Optional.ofNullable(u.getRoles())
                .orElseGet(Set::of)
                .stream().map(Role::getName).collect(Collectors.toSet());

        Set<String> perms = Optional.ofNullable(u.getRoles())
                .orElseGet(Set::of)
                .stream()
                .flatMap(r -> Optional.ofNullable(r.getPermissions()).orElseGet(Set::of).stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .username(u.getUsername())
                .phone(u.getPhone())
                .isActive(Boolean.TRUE.equals(u.getIsActive()))
                .isEmailVerified(Boolean.TRUE.equals(u.getIsEmailVerified()))
                .roles(roles)
                .permissions(perms)
                .build();
    }
}

