package com.ecomm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redis;
    private static final String KEY_PREFIX = "bl:jti:"; // blacklist jti

    public void blacklist(String jti, long expEpochSeconds) {
        long ttl = expEpochSeconds - (System.currentTimeMillis() / 1000);
        if (ttl <= 0) ttl = 1;
        String key = KEY_PREFIX + jti;
        redis.opsForValue().set(key, "1", Duration.ofSeconds(ttl));
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(KEY_PREFIX + jti));
    }
}

