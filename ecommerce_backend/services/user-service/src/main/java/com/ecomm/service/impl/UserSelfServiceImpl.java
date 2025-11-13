package com.ecomm.service.impl;

import com.ecomm.config.security.util.SecurityUtils;
import com.ecomm.entity.domain.EmailVerificationToken;
import com.ecomm.dto.request.ChangePasswordRequest;
import com.ecomm.dto.request.SelfUpdateRequest;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.User;
import com.ecomm.notification.SecurityEvents.AccountStatusChangedEvent;
import com.ecomm.notification.SecurityEvents.EmailVerificationRequestedEvent;
import com.ecomm.notification.SecurityEvents.PasswordChangedEvent;
import com.ecomm.repository.EmailVerificationTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.UserSelfService;
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSelfServiceImpl implements UserSelfService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final EmailVerificationTokenRepository emailTokenRepo;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "currentUser", key = "T(com.ecomm.config.security.util.SecurityUtils).currentUserEmail()")
    public UserResponse getCurrentUser() {
        User user = requireCurrent();
        events.publishEvent(new AccountStatusChangedEvent(user.getId(), user.getEmail(), user.getIsActive()));
        return UserMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = "currentUser", key = "T(com.ecomm.config.security.util.SecurityUtils).currentUserEmail()")
    public UserResponse updateCurrentUser(SelfUpdateRequest req) {
        User u = requireCurrent();
        if (req.getUsername() != null) u.setUsername(req.getUsername());
        if (req.getPhone() != null) u.setPhone(req.getPhone());
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (userRepo.existsByEmailIgnoreCase(req.getEmail()))
                throw new IllegalArgumentException("Email already in use");

            u.setEmail(req.getEmail());
            u.setIsEmailVerified(false);
            u.setTokenVersion(u.getTokenVersion() + 1); // invalidate all existing tokens

            String token = java.util.UUID.randomUUID().toString();
            emailTokenRepo.save(EmailVerificationToken.builder()
                    .token(token)
                    .user(u)
                    .expiresAt(java.time.Instant.now().plus(java.time.Duration.ofHours(24)))
                    .build());

            events.publishEvent(new EmailVerificationRequestedEvent(u.getId(), u.getEmail(), token));
        }
        return UserMapper.toResponse(u);
    }

    @Override
    @CacheEvict(value = "currentUser", key = "T(com.ecomm.config.security.util.SecurityUtils).currentUserEmail()")
    public void changePassword(ChangePasswordRequest req) {
        User user = requireCurrent();
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash()))
            throw new IllegalArgumentException("Old password is incorrect");
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        events.publishEvent(new PasswordChangedEvent(user.getId(), user.getEmail()));
    }

    @Override
    @CacheEvict(value = "currentUser", key = "T(com.ecomm.config.security.util.SecurityUtils).currentUserEmail()")
    public UserResponse patchStatus(boolean active) {
        User u = requireCurrent();
        u.setIsActive(active);
        events.publishEvent(new AccountStatusChangedEvent(u.getId(), u.getEmail(), active));
        return UserMapper.toResponse(u);
    }

    private User requireCurrent() {
        String email = SecurityUtils.currentUserEmail();
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
