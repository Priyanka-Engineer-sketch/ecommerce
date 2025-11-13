package com.ecomm.dto.request;


import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelfUpdateRequest {
    @Size(min=3, max=50) private String username;
    // allow email change only if you support re-verification; otherwise omit
    @Email private String email;
    @Pattern(regexp="^$|^[0-9+\\-() ]{7,20}$") private String phone;
}

