package com.ecomm.entity.domain.fraud;

import com.ecomm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@Slf4j
public class FraudScoreServiceImpl implements FraudScoreService {

    @Override
    public FraudRiskLevel evaluateLoginRisk(User user, String ip, String userAgent) {

        // VERY simple heuristic (placeholder for ML model):
        int score = 0;

        // 1. Logins at night (1am–5am) more suspicious
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(1, 0)) && now.isBefore(LocalTime.of(5, 0))) {
            score += 20;
        }

        // 2. Missing IP or UA => suspicious
        if (ip == null || ip.isBlank()) score += 30;
        if (userAgent == null || userAgent.isBlank()) score += 20;

        // 3. Not email verified => more suspicious
        if (!Boolean.TRUE.equals(user.getIsEmailVerified())) score += 15;

        // 4. Potentially add: new country, new device fingerprint, etc.

        FraudRiskLevel level;
        if (score >= 50) {
            level = FraudRiskLevel.HIGH;
        } else if (score >= 20) {
            level = FraudRiskLevel.MEDIUM;
        } else {
            level = FraudRiskLevel.LOW;
        }

        log.info("Fraud risk for user {}: score={} level={} ip={} agent={}",
                user.getEmail(), score, level, ip, userAgent);

        return level;
    }
}

