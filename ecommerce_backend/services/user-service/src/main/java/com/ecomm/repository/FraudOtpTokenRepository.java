package com.ecomm.repository;

import com.ecomm.entity.domain.otp.FraudOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FraudOtpTokenRepository extends JpaRepository<FraudOtpToken, Long> {
    Optional<FraudOtpToken> findByTokenAndConsumedFalse(String token);
}

