package com.ecomm.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "oauth_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String provider; // GOOGLE, FACEBOOK
    private String accessToken;
    private String refreshToken;
    private Instant expiresAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
