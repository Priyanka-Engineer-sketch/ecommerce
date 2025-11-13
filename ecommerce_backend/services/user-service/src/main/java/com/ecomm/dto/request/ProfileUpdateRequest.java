package com.ecomm.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ProfileUpdateRequest(
        @NotBlank String firstName,
        String lastName,
        String address,
        String city,
        String country,
        String postalCode
) {
}

