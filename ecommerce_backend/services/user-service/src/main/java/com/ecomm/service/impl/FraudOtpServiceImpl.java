package com.ecomm.service.impl;

import com.ecomm.dto.response.AuthResponse;
import com.ecomm.entity.User;
import com.ecomm.entity.domain.OtpToken;
import com.ecomm.entity.domain.OtpType;
import com.ecomm.entity.domain.otp.FraudOtpToken;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.FraudOtpTokenRepository;
import com.ecomm.repository.OtpTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.saga.kafka.UserEventProducer;
import com.ecomm.service.FraudOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class FraudOtpServiceImpl implements FraudOtpService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpRepo;
    private final EmailService emailService;
    private final AuthServiceImpl authServiceImpl; // reuse token builder
    private final FraudOtpTokenRepository fraudOtpRepo;
    private final UserEventProducer userEventProducer;

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXP_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 5;

    @Override
    public void createFraudOtpForLogin(User user, String ip, String userAgent) {
        String otp = String.valueOf(100000 + (int)(Math.random() * 900000));

        FraudOtpToken token = FraudOtpToken.builder()
                .token(otp)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();

        fraudOtpRepo.save(token);

        // you can create a dedicated template if you want
        emailService.sendHtmlEmail(
                user.getEmail(),
                "Verify suspicious login",
                """
                <h2>Verify this login</h2>
                <p>Hi %s,</p>
                <p>We detected a suspicious login attempt to your account.</p>
                <p>Your verification code is:</p>
                <p style="font-size:20px;font-weight:bold;">%s</p>
                <p>This code is valid for 10 minutes.</p>
                """.formatted(user.getUsername(), otp)
        );
    }

    @Override
    public void validateFraudOtp(String email, String otp) {
        var token = fraudOtpRepo.findByTokenAndConsumedFalse(otp)
                .orElseThrow(() -> new RuntimeException("Invalid or expired code"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Code expired");
        }

        if (!token.getUser().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Code does not match this user");
        }

        token.setConsumed(true);
        fraudOtpRepo.save(token);
    }

    @Override
    public AuthResponse verifyFraudOtp(String email, String otpCode) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OtpToken token = otpRepo.findTopByUserAndTypeAndConsumedFalseOrderByExpiresAtDesc(
                        user, OtpType.FRAUD_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("No active fraud OTP"));

        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (token.getAttempts() >= MAX_ATTEMPTS) {
            throw new RuntimeException("Too many attempts. Request a new login.");
        }

        if (!token.getOtp().equals(otpCode)) {
            token.setAttempts(token.getAttempts() + 1);
            otpRepo.save(token);
            throw new RuntimeException("Invalid OTP");
        }

        userEventProducer.sendFraudAlert(user, 0, "FRAUD_VERIFY_SUCCESS");

        token.setUsed(true);
        otpRepo.save(token);

        // now we trust the login and issue tokens
        return authServiceImpl.issueTokensForUser(user);
    }

    private String generateOtp() {
        SecureRandom rnd = new SecureRandom();
        int value = rnd.nextInt(900_000) + 100_000;
        return String.valueOf(value);
    }
}
