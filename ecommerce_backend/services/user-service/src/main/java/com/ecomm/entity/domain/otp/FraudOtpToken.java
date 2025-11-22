package com.ecomm.entity.domain.otp;

import com.ecomm.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "fraud_otp_tokens", indexes = @Index(columnList = "token"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class FraudOtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;        // 6-digit OTP

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean consumed = false;

    // optional meta
    private String ipAddress;
    private String userAgent;
    private Integer riskScore;
    private String email;
    private Instant createdAt;

}
