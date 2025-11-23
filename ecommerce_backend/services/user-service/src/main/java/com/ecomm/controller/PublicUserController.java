package com.ecomm.controller;

import com.ecomm.dto.response.PublicUserProfileResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.UserProfile;
import com.ecomm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/public")
@RequiredArgsConstructor
public class PublicUserController {

    private final UserRepository userRepository;

    @GetMapping("/{username:.+}")
    public ResponseEntity<PublicUserProfileResponse> getPublicProfile(
            @PathVariable String username
    ) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserProfile p = user.getProfile();

        if (p == null)
            throw new RuntimeException("Profile incomplete");

        PublicUserProfileResponse dto = PublicUserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(p.getDisplayName())
                .avatarUrl(p.getAvatarUrl())
                .city(p.getCity())
                .isSeller(user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SELLER")))
                .rating(p.getRating())
                .totalReviews(p.getTotalReviews())
                .joinedAt(p.getCreatedAt())
                .build();

        return ResponseEntity.ok(dto);
    }
}

