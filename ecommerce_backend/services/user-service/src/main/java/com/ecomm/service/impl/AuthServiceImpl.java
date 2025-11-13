package com.ecomm.service.impl;

import com.ecomm.config.security.JwtService;
import com.ecomm.config.security.SecurityFlags;
import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.exception.UserAlreadyExistsException;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.AuthService;
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
        u.setIsEmailVerified(flags.autoVerifyOnRegister); // true in tests
        u.setTokenVersion(0);
        // IMPORTANT: initialize roles collection to avoid NPEs
        u.setRoles(new HashSet<>(List.of(userRole)));

        User saved = userRepository.save(u);
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

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }
        return buildTokens(u);
    }

    @Override
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.validate(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String email = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Optional tokenVersion check (if you want rotation to invalidate old tokens)
        Integer ver = jwtService.extractTokenVersion(refreshToken);
        if (ver != null && !ver.equals(user.getTokenVersion())) {
            throw new RuntimeException("Refresh token expired");
        }
        return buildTokens(user);
    }

    @Cacheable(value = "usersByEmail", key = "#email") // ✅ cache user by email
    public User loadUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ===== helpers =====
    private AuthResponse buildTokens(User user) {
        Set<String> roles = safeRoles(user);
        Set<String> perms = safePerms(user);

        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .tokenType("Bearer")
                .expiresIn((int) jwtService.getAccessTokenValiditySeconds())
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .roles(roles)
                .permissions(perms)
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
