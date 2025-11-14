package com.ecomm.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

