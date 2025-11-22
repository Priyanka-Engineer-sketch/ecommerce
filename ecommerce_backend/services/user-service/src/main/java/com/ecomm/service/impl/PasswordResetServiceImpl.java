package com.ecomm.service.impl;

import com.ecomm.entity.User;
import com.ecomm.entity.domain.OtpToken;
import com.ecomm.entity.domain.OtpType;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.OtpTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.PasswordResetService;
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
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpRepo;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXP_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // generate 6-digit OTP
        String otp = generateOtp();

        Instant expiresAt = Instant.now().plus(OTP_EXP_MINUTES, ChronoUnit.MINUTES);

        OtpToken token = OtpToken.builder()
                .user(user)
                .type(String.valueOf(OtpType.PASSWORD_RESET))
                .otp(otp)
                .expiresAt(expiresAt)
                .used(false)
                .attempts(0)
                .build();

        otpRepo.save(token);

        // send email
        emailService.sendPasswordResetOtp(user, otp);
    }

    @Override
    public void resetPassword(String email, String otpCode, String newPassword) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OtpToken token = otpRepo.findTopByUserAndTypeAndConsumedFalseOrderByExpiresAtDesc(
                user, OtpType.PASSWORD_RESET
        ).orElseThrow(() -> new RuntimeException("No active reset OTP"));

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

        // mark consumed
        token.setUsed(true);
        otpRepo.save(token);

        // update password
        user.setPasswordHash(encoder.encode(newPassword));
        // optional: bump tokenVersion to invalidate existing refresh tokens
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.save(user);

        // send password changed email
        emailService.sendPasswordChangedEmail(user);
    }

    private String generateOtp() {
        SecureRandom rnd = new SecureRandom();
        int value = rnd.nextInt(900_000) + 100_000; // 100000–999999
        return String.valueOf(value);
    }
}
