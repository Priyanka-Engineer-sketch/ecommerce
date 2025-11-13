package com.ecomm.dto.response;

import com.ecomm.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private boolean isActive;
    private boolean isEmailVerified;
    private Set<String> roles;
    private Set<String> permissions;
}
