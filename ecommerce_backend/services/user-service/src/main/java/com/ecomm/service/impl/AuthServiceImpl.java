package com.ecomm.service.impl;

import com.ecomm.config.security.JwtService;
import com.ecomm.config.security.ai.LoginRiskEngine;
import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.entity.UserProfile;
import com.ecomm.entity.domain.fraud.FraudRiskLevel;
import com.ecomm.entity.domain.fraud.FraudScoreService;
import com.ecomm.exception.UserAlreadyExistsException;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserProfileRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.saga.kafka.UserEventProducer;
import com.ecomm.securitycommon.SecurityFlags;
import com.ecomm.service.*;
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityFlags flags;
    private final AsyncLoginSideEffects asyncLoginSideEffects;

    private final FraudScoreService fraudScoreService;
    private final FraudOtpService fraudOtpService;
    private final UserEventProducer userEventProducer; // ⭐ Kafka Producer
    private final LoginRiskEngine riskEngine;
    private final OtpService otpService;

    private final SecurityAuditServiceImpl auditService;

    // =====================================================================================
    // REGISTER
    // =====================================================================================
    @Override
    @CacheEvict(value = "usersByEmail", key = "#req.email")
    public AuthResponse register(RegisterRequest req) {

        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").description("Standard user").build()
                ));

        User u = new User();
        u.setEmail(req.getEmail());
        u.setUsername(req.getUsername());
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        u.setIsActive(true);
        u.setIsEmailVerified(flags.autoVerifyOnRegister);
        u.setTokenVersion(0);
        u.setRoles(new HashSet<>(List.of(userRole)));

        Instant now = Instant.now();

        UserProfile profile = UserProfile.builder()
                .displayName(req.getUsername())
                .createdAt(now)
                .updatedAt(now)
                .build();

        profile.setUser(u);
        u.setProfile(profile);

        User saved = userRepository.save(u);

        // ⭐ KAFKA EVENT
        userEventProducer.sendUserRegisteredEvent(saved);

        return buildTokens(saved);
    }

    // =====================================================================================
    // Register User by Admin
    // =====================================================================================
    @Override
    @CacheEvict(value = "usersByEmail", key = "#req.email")
    public UserResponse registerUser(RegisterRequest req) {

        if (userRepository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name("ROLE_USER").description("Standard user").build()
                ));

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .isActive(true)
                .roles(new HashSet<>(List.of(userRole)))
                .build();

        User saved = userRepository.save(user);

        // ⭐ KAFKA EVENT
        userEventProducer.sendUserRegisteredByAdminEvent(saved);

        return UserMapper.toResponse(saved);
    }

    // =====================================================================================
    // LOGIN
    // =====================================================================================
    @Override
    @Transactional
    public AuthResponse login(LoginRequest req, String ip, String userAgent) {

        User u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 1) Fraud scoring
        FraudRiskLevel risk = fraudScoreService.evaluateLoginRisk(u, ip, userAgent);

        if (risk == FraudRiskLevel.HIGH) {

            fraudOtpService.createFraudOtpForLogin(u, ip, userAgent);
            asyncLoginSideEffects.handleHighRiskLogin(u, ip, userAgent);

            // ⭐ KAFKA EVENT
            userEventProducer.sendUserFraudLoginEvent(u, ip, userAgent);

            throw new RuntimeException("High risk login detected. Verification code sent to your email.");
        }

        // 2) Lightweight risk engine
        int riskScore = riskEngine.calculateRisk(u.getEmail(), ip, userAgent);

        if (riskScore >= 60) {

            asyncLoginSideEffects.handleSuspiciousLogin(u, riskScore, ip, userAgent);

            // ⭐ KAFKA EVENT
            userEventProducer.sendUserSuspiciousLoginEvent(u, ip, userAgent, riskScore);

            return AuthResponse.builder()
                    .requires2FA(true)
                    .email(u.getEmail())
                    .message("Suspicious login detected")
                    .build();
        }

        // 3) Normal login
        AuthResponse tokens = buildTokens(u);

        asyncLoginSideEffects.handleSuccessfulLogin(u, riskScore, riskScore, ip, userAgent);

        // ⭐ KAFKA EVENT
        userEventProducer.sendUserLoginSuccessEvent(u, ip, userAgent);

        return tokens;
    }

    // =====================================================================================
    // REFRESH TOKEN
    // =====================================================================================
    @Override
    public AuthResponse refresh(String refreshToken) {

        if (!jwtService.validate(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String userIdString = jwtService.extractUsername(refreshToken);

        User user = userRepository.findById(Long.valueOf(userIdString))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer ver = jwtService.extractTokenVersion(refreshToken);
        if (ver != null && !ver.equals(user.getTokenVersion())) {
            throw new RuntimeException("Refresh token expired");
        }

        // ⭐ KAFKA EVENT
        userEventProducer.sendUserRefreshTokenEvent(user);

        return buildTokens(user);
    }

    @Override
    public AuthResponse issueTokensForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildTokens(user);
    }

    // =====================================================================================
    // LOGOUT ALL
    // =====================================================================================
    @Override
    public void logoutAll(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        // ⭐ KAFKA EVENT
        userEventProducer.sendLogoutAllEvent(userId);
    }

    // =====================================================================================
    // Helpers
    // =====================================================================================
    private AuthResponse buildTokens(User user) {

        Set<String> roles = safeRoles(user);
        Set<String> perms = safePerms(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenValiditySeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roles)
                .permissions(perms)
                .requires2FA(false)
                .message(null)
                .build();
    }

    private static Set<String> safeRoles(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private static Set<String> safePerms(User user) {
        if (user.getRoles() == null) return Set.of();
        return user.getRoles().stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getPermissions() == null
                        ? Stream.<String>empty()
                        : r.getPermissions().stream()
                        .filter(Objects::nonNull)
                        .map(p -> p.getName()))
                .collect(Collectors.toSet());
    }
}
