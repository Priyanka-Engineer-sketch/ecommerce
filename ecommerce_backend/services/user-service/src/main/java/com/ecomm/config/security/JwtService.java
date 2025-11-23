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

    // ===========================
    // JWT CONFIG FROM YAML
    // ===========================
    @Value("${security.jwt.hmac-secret}")
    private String secret;

    @Value("${security.jwt.access-exp-seconds:900}")
    private long accessExpSeconds;

    @Value("${security.jwt.refresh-exp-seconds:2592000}")
    private long refreshExpSeconds;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.audience}")
    private String audience;

    private Key key;

    // ===========================
    // KEY INITIALIZATION
    // ===========================
    @PostConstruct
    public void init() {

        if (secret == null || secret.isBlank()) {
            secret = "change-this-secret-please-32-characters-minimum";
        }

        byte[] keyBytes;

        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception e1) {
            try {
                keyBytes = Decoders.BASE64URL.decode(secret);
            } catch (Exception e2) {
                keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            }
        }

        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public long getAccessTokenValiditySeconds() {
        return accessExpSeconds;
    }

    // ===========================
    // ACCESS TOKEN
    // ===========================
    public String generateAccessToken(User user) {

        Map<String, Object> claims = new HashMap<>();

        // roles as array
        claims.put("roles",
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );

        // permissions as array
        claims.put("perms",
                user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getName)
                        .collect(Collectors.toSet())
        );

        claims.put("ver", user.getTokenVersion());
        claims.put("typ", "access");
        claims.put("email", user.getEmail());
        claims.put("uid", user.getId());

        return buildToken(
                user.getId().toString(), // 🔥 subject = userId (GATEWAY EXPECTS THIS)
                claims,
                accessExpSeconds
        );
    }

    // ===========================
    // REFRESH TOKEN
    // ===========================
    public String generateRefreshToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("ver", user.getTokenVersion());
        claims.put("typ", "refresh");
        claims.put("email", user.getEmail());
        claims.put("uid", user.getId());

        return buildToken(
                user.getId().toString(), // 🔥 must be userId
                claims,
                refreshExpSeconds
        );
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = Map.of("typ", "refresh");
        return buildToken(email, claims, refreshExpSeconds);
    }

    // ===========================
    // TOKEN VALIDATION
    // ===========================
    public boolean validate(String token) {
        try {
            Jws<Claims> jws = parse(token);
            Date exp = jws.getBody().getExpiration();
            return exp != null && !exp.before(new Date());
        } catch (ExpiredJwtException e) {
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parse(token).getBody().get("typ"));
        } catch (Exception e) {
            return false;
        }
    }

    public Integer extractTokenVersion(String token) {
        try {
            Object v = parse(token).getBody().get("ver");
            if (v instanceof Integer) return (Integer) v;
            if (v instanceof Number) return ((Number) v).intValue();
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String extractUsername(String token) {
        return parse(token).getBody().getSubject();
    }

    // ===========================
    // INTERNAL JWT BUILDER
    // ===========================
    private String buildToken(String subject, Map<String, Object> claims, long ttlSeconds) {

        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlSeconds * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)

                // 🔥 MUST match Gateway’s JwtAuth required fields
                .setIssuer(issuer)
                .setAudience(audience)

                .setIssuedAt(now)
                .setExpiration(exp)

                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ===========================
    // PARSE WITH SIGNATURE CHECK
    // ===========================
    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(60)
                .build()
                .parseClaimsJws(token);
    }
}
