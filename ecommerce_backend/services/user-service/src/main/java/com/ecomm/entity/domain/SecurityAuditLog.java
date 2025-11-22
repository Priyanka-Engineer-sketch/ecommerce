package com.ecomm.entity.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "security_audit_logs", indexes = {
        @Index(columnList = "email"),
        @Index(columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 150)
    private String email;

    @Column(nullable = false, length = 100)
    private String action;  // LOGIN_SUCCESS, LOGIN_FAILED, PASSWORD_CHANGED, PROFILE_UPDATED, ROLE_CHANGED...

    @Column(length = 500)
    private String details;

    @Column(length = 64)
    private String ip;

    @Column(length = 255)
    private String userAgent;

    @Column(nullable = false)
    private Instant createdAt;
}
