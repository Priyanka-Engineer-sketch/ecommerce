package com.ecomm.service.impl;

import com.ecomm.dto.request.LoginRequest;
import com.ecomm.dto.response.AuthResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.domain.OtpToken;
import com.ecomm.entity.domain.OtpType;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.OtpTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.TwoFactorAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class TwoFactorAuthServiceImpl implements TwoFactorAuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthServiceImpl authServiceImpl; // concrete, to reuse token issuance

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXP_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void requestLoginOtp(LoginRequest req, String ip, String userAgent) {
        User user = userRepository.findByEmailIgnoreCase(req.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new RuntimeException("Account is disabled");
        }

        // generate OTP
        String otp = generateOtp();
        Instant expiresAt = Instant.now().plus(OTP_EXP_MINUTES, ChronoUnit.MINUTES);

        OtpToken token = OtpToken.builder()
                .user(user)
                .type(String.valueOf(OtpType.LOGIN_2FA))
                .otp(otp)
                .expiresAt(expiresAt)
                .used(false)
                .attempts(0)
                .build();

        otpRepo.save(token);

        // send 2FA email
        emailService.sendLogin2faOtp(user, otp, ip, userAgent);
    }

    @Override
    public AuthResponse verifyLoginOtp(String email, String otpCode) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OtpToken token = otpRepo.findTopByUserAndTypeAndConsumedFalseOrderByExpiresAtDesc(
                user, OtpType.LOGIN_2FA
        ).orElseThrow(() -> new RuntimeException("No active login OTP"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new RuntimeException("Too many attempts. Request a new OTP.");
        }

        if (!token.getOtp().equals(otpCode)) {
            token.setAttempts(token.getAttempts() + 1);
            otpRepo.save(token);
            throw new RuntimeException("Invalid OTP");
        }

        token.setUsed(true);
        otpRepo.save(token);

        // issue JWT tokens for successful login
        return authServiceImpl.issueTokensForUser(user.getEmail());
    }

    private String generateOtp() {
        SecureRandom rnd = new SecureRandom();
        int value = rnd.nextInt(900_000) + 100_000; // 100000–999999
        return String.valueOf(value);
    }
}
