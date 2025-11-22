package com.ecomm.repository;

import com.ecomm.entity.domain.otp.FraudOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudOtpTokenRepository extends JpaRepository<FraudOtpToken, Long> {
    Optional<FraudOtpToken> findByTokenAndConsumedFalse(String token);
}

