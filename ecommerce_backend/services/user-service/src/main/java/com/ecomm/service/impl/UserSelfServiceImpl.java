package com.ecomm.service.impl;

import com.ecomm.dto.request.ChangePasswordRequest;
import com.ecomm.dto.request.SelfUpdateRequest;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.domain.EmailVerificationToken;
import com.ecomm.notification.EmailService;
import com.ecomm.notification.SecurityEvents.AccountStatusChangedEvent;
import com.ecomm.notification.SecurityEvents.EmailVerificationRequestedEvent;
import com.ecomm.notification.SecurityEvents.PasswordChangedEvent;
import com.ecomm.repository.EmailVerificationTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.securitycommon.SecurityUtils;
import com.ecomm.service.SecurityAuditService;
import com.ecomm.service.UserSelfService;
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserSelfServiceImpl implements UserSelfService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final EmailVerificationTokenRepository emailTokenRepo;
    private final EmailService emailService;
    private final SecurityAuditService auditService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "currentUser",
            key = "T(com.ecomm.securitycommon.SecurityUtils).currentUserEmail()")
    public UserResponse getCurrentUser() {
        User user = requireCurrent();
        // If you want only on status change you can remove this,
        // but keeping as per your existing logic
        events.publishEvent(new AccountStatusChangedEvent(
                user.getId(),
                user.getEmail(),
                user.getIsActive()
        ));
        return UserMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = "currentUser",
            key = "T(com.ecomm.securitycommon.SecurityUtils).currentUserEmail()")
    public UserResponse updateCurrentUser(SelfUpdateRequest req) {
        User u = requireCurrent();

        // username / phone updates
        if (req.getUsername() != null) {
            u.setUsername(req.getUsername());
        }
        if (req.getPhone() != null) {
            u.setPhone(req.getPhone());
        }

        // email update – requires re-verification + token invalidation
        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (userRepo.existsByEmailIgnoreCase(req.getEmail())) {
                throw new IllegalArgumentException("Email already in use");
            }

            u.setEmail(req.getEmail());
            u.setIsEmailVerified(false);
            // invalidate existing tokens
            u.setTokenVersion(u.getTokenVersion() + 1);

            String token = UUID.randomUUID().toString();
            emailTokenRepo.save(EmailVerificationToken.builder()
                    .token(token)
                    .user(u)
                    .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                    .consumed(false)
                    .build());

            // publish domain event (for Kafka / listeners)
            events.publishEvent(new EmailVerificationRequestedEvent(
                    u.getId(),
                    u.getEmail(),
                    token
            ));

            // send verification email directly
            emailService.sendVerificationEmail(u, token);
        }

        User saved = userRepo.save(u);

        // send profile-updated email for any change
        emailService.sendProfileUpdatedEmail(saved);
        auditService.log(u, "PROFILE_UPDATED", "User updated own profile", null, null);

        return UserMapper.toResponse(saved);
    }

    @Override
    @CacheEvict(value = "currentUser",
            key = "T(com.ecomm.securitycommon.SecurityUtils).currentUserEmail()")
    public void changePassword(ChangePasswordRequest req) {
        User user = requireCurrent();

        if (!passwordEncoder.matches(req.getOldPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        // bump tokenVersion so existing refresh tokens become invalid
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepo.save(user);

        // publish event
        events.publishEvent(new PasswordChangedEvent(
                user.getId(),
                user.getEmail()
        ));
        auditService.log(user, "PASSWORD_CHANGED", "Self-service change", null, null);

        // send email notification
        emailService.sendPasswordChangedEmail(user);
    }

    @Override
    @CacheEvict(value = "currentUser",
            key = "T(com.ecomm.securitycommon.SecurityUtils).currentUserEmail()")
    public UserResponse patchStatus(boolean active) {
        User u = requireCurrent();
        u.setIsActive(active);
        User saved = userRepo.save(u);

        events.publishEvent(new AccountStatusChangedEvent(
                saved.getId(),
                saved.getEmail(),
                active
        ));

        return UserMapper.toResponse(saved);
    }

    // ---------- helper ----------

    private User requireCurrent() {
        String email = SecurityUtils.currentUserEmail();
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }
}
