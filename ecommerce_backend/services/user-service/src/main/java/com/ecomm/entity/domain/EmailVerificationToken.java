package com.ecomm.entity.domain;

import com.ecomm.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="email_verification_tokens", indexes=@Index(columnList="token", unique=true))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @Column(nullable=false, unique=true) private String token;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    private User user;

    @Column(nullable=false) private Instant expiresAt;
    @Column(nullable=false) private boolean consumed = false;
}
