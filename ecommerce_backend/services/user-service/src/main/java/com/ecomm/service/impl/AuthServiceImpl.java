package com.ecomm.service.impl;

import com.ecomm.config.security.JwtService;
import com.ecomm.config.security.ai.LoginRiskEngine;
import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.entity.domain.fraud.FraudRiskLevel;
import com.ecomm.entity.domain.fraud.FraudScoreService;
import com.ecomm.exception.UserAlreadyExistsException;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.saga.kafka.UserEventProducer;
import com.ecomm.securitycommon.SecurityFlags;
import com.ecomm.service.AuthService;
import com.ecomm.service.FraudOtpService;
import com.ecomm.service.OtpService;          // <-- use interface, not impl
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager; // ok if unused
    private final SecurityFlags flags;
    private final EmailService emailService;

    // fraud / risk / kafka / otp
    private final FraudScoreService fraudScoreService;
    private final FraudOtpService fraudOtpService;
    private final UserEventProducer userEventProducer;
    private final LoginRiskEngine riskEngine;
    private final OtpService otpService;

    private final SecurityAuditServiceImpl auditService;
    // ------------------------------------------------------------------------
    // Registration
    // ------------------------------------------------------------------------

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
        u.setIsEmailVerified(flags.autoVerifyOnRegister); // true in tests/dev
        u.setTokenVersion(0);
        u.setRoles(new HashSet<>(List.of(userRole)));

        User saved = userRepository.save(u);
        // you can also send welcome email + kafka event here if you want
        return buildTokens(saved);
    }

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

        return UserMapper.toResponse(userRepository.save(user));
    }

    // ------------------------------------------------------------------------
    // Login with fraud scoring + 2FA
    // ------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req, String ip, String userAgent) {
        User u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 1) AI-style fraud scoring (HIGH -> hard block + fraud OTP)
        FraudRiskLevel risk = fraudScoreService.evaluateLoginRisk(u, ip, userAgent);
        if (risk == FraudRiskLevel.HIGH) {
            // Do NOT issue tokens. Require special fraud verification OTP.
            fraudOtpService.createFraudOtpForLogin(u, ip, userAgent);
            throw new RuntimeException(
                    "High risk login detected. Verification code sent to your email.");
        }

        // 2) Lightweight heuristic risk engine (e.g. device/ip anomalies)
        int riskScore = riskEngine.calculateRisk(u.getEmail(), ip, userAgent);
        int riskCounter = riskEngine.calculateRisk(u.getEmail(), ip, userAgent);

        if (riskCounter >= 60) {
            otpService.sendOtp(u.getEmail(), "FRAUD_VERIFY");
            userEventProducer.sendFraudAlert(u, riskCounter,"FRAUD_VERIFY");
            auditService.log(u, "LOGIN_SUSPICIOUS",
                    "RiskScore=" + riskCounter,
                    ip, userAgent);
            return AuthResponse.builder()
                    .requires2FA(true)
                    .message("Suspicious login detected")
                    .email(u.getEmail())
                    .build();
        }

        // 3) Normal login: send notifications + kafka, then issue tokens
        emailService.sendLoginNotification(u, ip, userAgent);
        userEventProducer.sendLoginEvent(u, ip, userAgent,riskScore);
        auditService.log(u, "LOGIN_SUCCESS",
                "RiskScore=" + riskCounter,
                ip, userAgent);

        return buildTokens(u);
    }

    // ------------------------------------------------------------------------
    // Refresh
    // ------------------------------------------------------------------------

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.validate(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Integer ver = jwtService.extractTokenVersion(refreshToken);
        if (ver != null && !ver.equals(user.getTokenVersion())) {
            throw new RuntimeException("Refresh token expired");
        }
        return buildTokens(user);
    }

    // ------------------------------------------------------------------------
    // Helpers / cache
    // ------------------------------------------------------------------------

    @Cacheable(value = "usersByEmail", key = "#email")
    public User loadUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private AuthResponse buildTokens(User user) {
        Set<String> roles = safeRoles(user);
        Set<String> perms = safePerms(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user.getEmail());

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

    // Used by /login/2fa and /login/fraud-verify after OTP is validated
    @Override
    public AuthResponse issueTokensForUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildTokens(user);
    }

    // optional overload if you already have the entity
    public AuthResponse issueTokensForUser(User user) {
        return buildTokens(user);
    }
}
