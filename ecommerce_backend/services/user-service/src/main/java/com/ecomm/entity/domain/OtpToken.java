package com.ecomm.entity.domain;

import com.ecomm.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(columnList = "otp"),
        @Index(columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;          // user email (redundant but helps)

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private String type;           // "FORGOT_PASSWORD", "FRAUD_CHECK", "LOGIN_2FA"

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts = 0;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private boolean used = false;

    private Instant createdAt;
}
