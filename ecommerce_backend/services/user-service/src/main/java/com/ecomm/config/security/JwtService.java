package com.ecomm.config.security;

import com.ecomm.entity.Permission;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Value("${app.jwt.secret:}")                       // Base64 or plain string
    private String secret;

    @Value("${app.jwt.access-exp-seconds:900}")        // 15 minutes (in SECONDS)
    private long accessExpSeconds;

    @Value("${app.jwt.refresh-exp-seconds:2592000}")   // 30 days (in SECONDS)
    private long refreshExpSeconds;

    private Key key;

    @PostConstruct
    public void init() {
        // Fallback secret for dev/test (don’t use in prod)
        final String material = (secret == null || secret.isBlank())
                ? "test-secret-please-change"
                : secret;

        byte[] keyBytes = null;

        // 1) Try standard Base64
        try {
            keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(material);
        } catch (RuntimeException ignored) {
            // 2) Try Base64URL (handles '-' and '_')
            try {
                keyBytes = io.jsonwebtoken.io.Decoders.BASE64URL.decode(material);
            } catch (RuntimeException ignored2) {
                // 3) Treat as raw text
                keyBytes = material.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            }
        }

        // HS256 requires >= 256-bit key (32 bytes)
        if (keyBytes.length < 32) {
            keyBytes = java.util.Arrays.copyOf(keyBytes, 32);
        }

        this.key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }


    public long getAccessTokenValiditySeconds() {
        return accessExpSeconds;
    }

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // Ensure your Role names are ROLE_* already; if not, prefix here
        claims.put("roles", user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()));
        claims.put("perms", user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet()));
        claims.put("ver", user.getTokenVersion());
        claims.put("typ", "access");
        return buildToken(user.getEmail(), claims, accessExpSeconds); // seconds
    }

    public String generateRefreshToken(User user) {
        Map<String, Object> claims = Map.of("ver", user.getTokenVersion(), "typ", "refresh");
        return buildToken(user.getEmail(), claims, refreshExpSeconds); // seconds
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = Map.of("typ", "refresh");
        return buildToken(email, claims, refreshExpSeconds); // seconds
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    public boolean isRefreshToken(String token) {
        try {
            Object typ = parse(token).getBody().get("typ");
            return "refresh".equals(typ);
        } catch (Exception e) {
            return false;
        }
    }

    public Integer extractTokenVersion(String token) {
        try {
            Object v = parse(token).getBody().get("ver");
            return (v instanceof Integer i) ? i : (v instanceof Number n ? n.intValue() : null);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validate(String token) {
        try {
            // parse() will verify signature & (with skew) expiration
            Jws<Claims> jws = parse(token);
            Date exp = jws.getBody().getExpiration();
            return exp != null && !exp.before(new Date());
        } catch (ExpiredJwtException e) {
            // token truly expired
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // malformed, bad signature, etc.
            return false;
        }
    }

    // ---------- internals ----------
    private String buildToken(String subject, Map<String, Object> claims, long ttlSeconds) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlSeconds * 1000L); // seconds -> ms
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)                 // <— use the Key
                .setAllowedClockSkewSeconds(60)     // optional skew
                .build()
                .parseClaimsJws(token);
    }
}
