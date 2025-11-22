package com.ecomm.service.impl;

import com.ecomm.entity.User;
import com.ecomm.entity.domain.otp.PasswordResetToken;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.PasswordResetTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void sendPasswordResetOtp(String email) {
        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));

        PasswordResetToken reset = PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .build();

        tokenRepo.save(reset);

        emailService.sendPasswordResetEmail(user, otp);
    }

    @Override
    public void resetPassword(String otp, String newPassword) {
        var token = tokenRepo.findByTokenAndConsumedFalse(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);

        token.setConsumed(true);
    }
}
