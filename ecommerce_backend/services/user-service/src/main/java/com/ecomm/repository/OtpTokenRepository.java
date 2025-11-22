package com.ecomm.repository;

import com.ecomm.entity.User;
import com.ecomm.entity.domain.OtpToken;
import com.ecomm.entity.domain.OtpType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByUserAndTypeAndConsumedFalseOrderByExpiresAtDesc(
            User user,
            OtpType type
    );

    Optional<OtpToken> findByEmailAndOtpAndUsedFalse(String email, String otp);

    long deleteByExpiresAtBefore(Instant cutoff);

    Optional<OtpToken> findTopByEmailAndTypeAndConsumedFalseOrderByExpiresAtDesc(
            String email, String type
    );
}
