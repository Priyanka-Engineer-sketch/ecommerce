package com.ecomm.controller;


import com.ecomm.entity.domain.EmailVerificationToken;
import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.User;
import com.ecomm.notification.SecurityEvents;
import com.ecomm.repository.EmailVerificationTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.ecomm.dto.response.MeResponse;

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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @Operation(summary = "Get current authenticated user (from JWT)")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(Authentication auth) {
        // auth.getName() is the username we used in JWT subject — your email
        String email = auth.getName();

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // roles (ROLE_*) and permissions from DB (authorities also available in auth)
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

    @PostMapping("/registerUserByAdmin")
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest req) {
        return authService.registerUser(req);
    }
    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam String token) {
        var ev = tokenRepo.findByTokenAndConsumedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));
        if (ev.getExpiresAt().isBefore(Instant.now()))
            throw new IllegalArgumentException("Token expired");

        User u = ev.getUser();
        u.setIsEmailVerified(true);
        ev.setConsumed(true);
        return "Email verified successfully";
    }

    @PostMapping("/resend-verification")
    public void resend(@RequestParam String email) {
        User u = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        if (u.getIsEmailVerified()) return;

        String token = UUID.randomUUID().toString();
        tokenRepo.save(EmailVerificationToken.builder()
                .token(token)
                .user(u)
                .expiresAt(Instant.now().plus(Duration.ofHours(24)))
                .build());

        events.publishEvent(new SecurityEvents.EmailVerificationRequestedEvent(u.getId(), u.getEmail(), token));
    }
}
