package com.ecomm.service;

import com.ecomm.entity.User;
import com.ecomm.notification.EmailService;
import com.ecomm.saga.kafka.UserEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncLoginSideEffects {

    private final EmailService emailService;
    private final UserEventProducer userEventProducer;
    private final SecurityAuditService auditService;
    private final OtpService otpService;

    @Async
    public void handleHighRiskLogin(User u, String ip, String userAgent) {
        try {
            otpService.sendOtp(u.getEmail(), "FRAUD_HIGH");
            userEventProducer.sendFraudAlert(u, 100, "FRAUD_HIGH");
            auditService.log(u, "LOGIN_HIGH_RISK", "High fraud risk", ip, userAgent);
        } catch (Exception ex) {
            // log and swallow, don't affect login thread
        }
    }

    @Async
    public void handleSuspiciousLogin(User u, int riskCounter, String ip, String userAgent) {
        try {
            otpService.sendOtp(u.getEmail(), "FRAUD_VERIFY");
            userEventProducer.sendFraudAlert(u, riskCounter, "FRAUD_VERIFY");
            auditService.log(u, "LOGIN_SUSPICIOUS", "RiskScore=" + riskCounter, ip, userAgent);
        } catch (Exception ex) {
            // log only
        }
    }

    @Async
    public void handleSuccessfulLogin(User u, int riskCounter, int riskScore, String ip, String userAgent) {
        try {
            emailService.sendLoginNotification(u, ip, userAgent);
            userEventProducer.sendLoginEvent(u, ip, userAgent, riskScore);
            auditService.log(u, "LOGIN_SUCCESS", "RiskScore=" + riskCounter, ip, userAgent);
        } catch (Exception ex) {
            // log only
        }
    }
}
