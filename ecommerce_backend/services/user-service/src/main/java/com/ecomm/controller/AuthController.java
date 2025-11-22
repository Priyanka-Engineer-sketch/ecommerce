package com.ecomm.controller;

import com.ecomm.dto.TokenRefreshRequest;
import com.ecomm.dto.request.*;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.MeResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.domain.EmailVerificationToken;
import com.ecomm.notification.EmailService;
import com.ecomm.notification.SecurityEvents;
import com.ecomm.repository.EmailVerificationTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Auth")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepo;
    private final ApplicationEventPublisher events;

    private final PasswordResetService passwordResetService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final FraudOtpService fraudOtpService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    // ---------------- REGISTER ----------------

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/registerUserByAdmin")
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest req) {
        return authService.registerUser(req);
    }

    // ---------------- REFRESH TOKEN (PUBLIC) ----------------

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
    }

    // ---------------- CURRENT USER ----------------

    @Operation(summary = "Get current authenticated user (from JWT)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication auth) {
        String email = auth.getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        Set<String> perms = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                MeResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .roles(roles)
                        .permissions(perms)
                        .build()
        );
    }

    // ---------------- EMAIL VERIFY ----------------

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        EmailVerificationToken ev = tokenRepo.findByTokenAndConsumedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (ev.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Token expired");
        }

        User u = ev.getUser();
        u.setIsEmailVerified(true);
        ev.setConsumed(true);
        return "Email verified successfully";
    }

    @PostMapping("/resend-verification")
    public void resend(@RequestParam String email) {
        User u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (Boolean.TRUE.equals(u.getIsEmailVerified())) return;

        String token = UUID.randomUUID().toString();
        tokenRepo.save(EmailVerificationToken.builder()
                .token(token)
                .user(u)
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build());

        events.publishEvent(
                new SecurityEvents.EmailVerificationRequestedEvent(u.getId(), u.getEmail(), token));
    }

    // ---------------- NORMAL LOGIN + AI RISK ----------------

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                              HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String agent = httpReq.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(req, ip, agent));
    }

    // ---------------- 2FA LOGIN (explicit OTP) ----------------

    @PostMapping("/login-2fa/request")
    public void login2faRequest(@Valid @RequestBody LoginRequest req,
                                HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        String agent = httpReq.getHeader("User-Agent");
        twoFactorAuthService.requestLoginOtp(req, ip, agent);
    }

    @PostMapping("/login-2fa/verify")
    public ResponseEntity<AuthResponse> login2faVerify(@Valid @RequestBody LoginOtpVerifyRequest req) {
        return ResponseEntity.ok(twoFactorAuthService.verifyLoginOtp(req.email(), req.otp()));
    }

    // Generic 2FA login using OTPService + AuthService.issueTokensForUser
    @PostMapping("/login/2fa")
    public ResponseEntity<AuthResponse> login2FA(@Valid @RequestBody OtpLoginRequest req) {
        otpService.validateOtp(req.email(), req.otp(), "LOGIN_2FA");
        AuthResponse response = authService.issueTokensForUser(req.email());
        return ResponseEntity.ok(response);
    }

    // ---------------- FORGOT PASSWORD (OTP) ----------------

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.sendPasswordResetOtp(user);
        return ResponseEntity.ok("OTP sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {

        otpService.validateOtp(req.email(), req.otp(), "PASSWORD_RESET");

        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangedEmail(user);

        return ResponseEntity.ok("Password reset successful");
    }

    // ---------------- FRAUD LOGIN OTP ----------------

    @PostMapping("/login-fraud/verify")
    public ResponseEntity<AuthResponse> verifyFraudOtp(@Valid @RequestBody FraudOtpVerifyRequest req) {
        fraudOtpService.validateFraudOtp(req.email(), req.otp());
        AuthResponse response = authService.issueTokensForUser(req.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/fraud-verify")
    public ResponseEntity<AuthResponse> fraudVerify(@Valid @RequestBody FraudOtpRequest req) {
        otpService.validateOtp(req.email(), req.otp(), "FRAUD_VERIFY");
        AuthResponse response = authService.issueTokensForUser(req.email());
        return ResponseEntity.ok(response);
    }
}
