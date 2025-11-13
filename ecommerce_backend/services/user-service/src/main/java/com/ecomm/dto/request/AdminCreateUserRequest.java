package com.ecomm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {
    @NotBlank
    @Size(min=3, max=50) private String username;
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min=8, max=100) private String password;
    private String phone;
    // optional explicit roles from admin, default ROLE_USER
    private java.util.Set<@Pattern(regexp="ROLE_[A-Z_]+") String> roles;
    private Boolean active; // optional
}

