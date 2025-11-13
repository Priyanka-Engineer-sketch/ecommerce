package com.ecomm.controller;

import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.ProfileUpdateRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.TokenRefreshRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.MeResponse;
import com.ecomm.dto.response.UserProfileResponse;
import com.ecomm.service.AuthService;
import com.ecomm.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService; // or private final UserProfileService userService;

    // Register (public)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Login (public)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Refresh (public)
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody TokenRefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.getRefreshToken()));
    }

    // Get profile (JWT protected)
    @GetMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    // Update profile (JWT protected)
    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(@PathVariable Long userId,
                                                             @Valid @RequestBody ProfileUpdateRequest req) {
        return ResponseEntity.ok(userService.updateProfile(userId, req));
    }
}
