package com.ecomm.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data @Builder
public class PublicUserProfileResponse {

    private Long id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String city;
    private Boolean isSeller;
    private Double rating;
    private Integer totalReviews;
    private Instant joinedAt;
}

