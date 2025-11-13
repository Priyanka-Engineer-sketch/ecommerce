package com.ecomm.service;

import com.ecomm.entity.User;

public interface JwtService {
    String generateAccessToken(User user);
    String generateRefreshToken(User user);

    // For existing code that used a single-arg method
    boolean validateToken(String token);

    // Newer call sites can specify access vs refresh explicitly
    boolean validateToken(String token, boolean refresh);

    String extractUsername(String token);
    String extractJti(String token);
    Integer extractTokenVersion(String token);
    Long extractExpiryEpochSeconds(String token);
}
