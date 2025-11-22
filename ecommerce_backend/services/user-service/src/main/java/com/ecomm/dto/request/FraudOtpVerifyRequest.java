package com.ecomm.dto.request;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record FraudOtpVerifyRequest(
        @NotBlank @Email String email,
        @NotBlank String otp
) {}