package com.ecomm.service.impl;

import com.ecomm.dto.request.ProfileUpdateRequest;
import com.ecomm.dto.response.MeResponse;
import com.ecomm.dto.response.UserProfileResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.UserProfile;
import com.ecomm.exception.ResourceNotFoundException;
import com.ecomm.repository.UserProfileRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId=" + userId));
        return toDto(profile);
    }

    @Override
    @CacheEvict(value = "userProfiles", key = "#userId") // ✅ Evict cache when profile updated
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found id=" + userId));

        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElse(UserProfile.builder().user(user).build());

        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setAddress(request.address());
        profile.setCity(request.city());
        profile.setCountry(request.country());
        profile.setPostalCode(request.postalCode());

        UserProfile saved = userProfileRepository.save(profile);
        return toDto(saved);
    }

    @Override
    @CacheEvict(value = "userProfiles", key = "#userId")
    public void deleteProfile(Long userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found for userId=" + userId));
        userProfileRepository.delete(profile);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "meResponse", key = "#root.authentication?.name")
    public MeResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new ResourceNotFoundException("No authenticated user in context");
        }

        // We use email as username in AuthServiceImpl/JWT
        String email = auth.getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email=" + email));

        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toSet());

        Set<String> perms = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return MeResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isActive(Boolean.TRUE.equals(user.getIsActive()))
                .roles(roles)
                .permissions(perms)
                .build();
    }

    private UserProfileResponse toDto(UserProfile profile) {
        return UserProfileResponse.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .address(profile.getAddress())
                .city(profile.getCity())
                .country(profile.getCountry())
                .postalCode(profile.getPostalCode())
                .userId(profile.getUser() != null ? profile.getUser().getId() : null)
                .build();
    }
}
