package com.ecomm.service.impl;

import com.ecomm.entity.User;
import com.ecomm.entity.domain.OtpToken;
import com.ecomm.notification.EmailService;
import com.ecomm.repository.OtpTokenRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.saga.kafka.UserEventProducer;
import com.ecomm.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final UserRepository userRepo;
    private final OtpTokenRepository otpRepo;
    private final EmailService emailService;
    private static final int OTP_EXP_MIN = 10;
    private final UserEventProducer userEventProducer;

    private String generateOtp() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    // ---------------- FORGOT PASSWORD OTP ----------------

    @Override
    public void sendForgotPasswordOtp(String email) {
        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        otpRepo.save(OtpToken.builder()
                .email(email)
                .otp(otp)
                .user(user)
                .type("FORGOT_PASSWORD")
                .expiresAt(Instant.now().plus(Duration.ofMinutes(10)))
                .build());

        emailService.sendForgotPasswordOtp(user, otp);
    }

    @Override
    public void verifyForgotPasswordOtp(String email, String otp) {
        var token = otpRepo.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("OTP expired");

        token.setUsed(true);
        otpRepo.save(token);
    }

    // ---------------- FRAUD CHECK OTP ----------------

    @Override
    public void sendFraudOtp(String email) {
        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        otpRepo.save(OtpToken.builder()
                .email(email)
                .otp(otp)
                .user(user)
                .type("FRAUD_CHECK")
                .expiresAt(Instant.now().plus(Duration.ofMinutes(15)))
                .build());

        emailService.sendFraudAlertOtp(user, otp);
    }

    @Override
    public void verifyFraudOtp(String email, String otp) {
        var token = otpRepo.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("OTP expired");

        token.setUsed(true);
        otpRepo.save(token);
    }

    public String generateOtp(int digits) {
        int max = (int) Math.pow(10, digits);
        int code = new Random().nextInt(max - 1) + 1;
        return String.format("%0" + digits + "d", code);
    }

    public void sendPasswordResetOtp(User user) {
        sendOtp(user, "PASSWORD_RESET");
    }

    public void sendLogin2faOtp(User user) {
        sendOtp(user, "LOGIN_2FA");
    }

    public void sendFraudOtp(User user) {
        sendOtp(user, "FRAUD_VERIFY");
    }

    void sendOtp(User user, String type) {
        String otp = generateOtp(6);

        otpRepo.save(OtpToken.builder()
                .email(user.getEmail())
                .user(user)
                .otp(otp)
                .type(type)
                .expiresAt(Instant.now().plus(Duration.ofMinutes(OTP_EXP_MIN)))
                .used(false)
                .build()
        );

        emailService.sendOtpEmail(user.getEmail(), otp, OTP_EXP_MIN);
    }

    public boolean validateOtp(String email, String otp, String type) {
        var token = otpRepo.findTopByEmailAndTypeAndConsumedFalseOrderByExpiresAtDesc(email, type)
                .orElseThrow(() -> new RuntimeException("Invalid OTP"));

        if (token.getExpiresAt().isBefore(Instant.now()))
            throw new RuntimeException("OTP expired");

        if (!token.getOtp().equals(otp))
            throw new RuntimeException("Invalid OTP");
        if(Objects.equals(type, "PASSWORD_RESET")){
            userEventProducer.sendPasswordReset(getUser(email));
        }
        token.setUsed(true);
        otpRepo.save(token);
        return true;
    }

    @Override
    public void sendOtp(String email, String purpose) {
        sendOtp(getUser(email), purpose);
    }

    private User getUser(String email) {
        return userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
