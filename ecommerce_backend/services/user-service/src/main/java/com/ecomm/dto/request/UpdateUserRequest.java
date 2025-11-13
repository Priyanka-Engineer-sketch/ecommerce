package com.ecomm.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    @Size(min = 3, max = 50)
    private String username;
    @Email
    private String email;
    @Size(min = 8, max = 100)
    private String password; // optional password change
    @Pattern(regexp = "^$|^[0-9+\\-() ]{7,20}$")
    private String phone;
    private Boolean active;
}