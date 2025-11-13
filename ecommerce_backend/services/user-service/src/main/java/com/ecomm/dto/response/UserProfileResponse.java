package com.ecomm.dto.response;

import lombok.Builder;

@Builder
public record UserProfileResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String address,
        String city,
        String country,
        String postalCode
) {}