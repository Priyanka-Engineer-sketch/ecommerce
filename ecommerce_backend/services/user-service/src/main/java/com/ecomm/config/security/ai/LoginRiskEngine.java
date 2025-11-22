package com.ecomm.config.security.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class LoginRiskEngine {

    @Data
    @AllArgsConstructor
    private static class LastLoginInfo {
        String ip;
        String userAgent;
        long ts;
    }

    private final Map<String, LastLoginInfo> lastLoginByUser = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> loginTimesByUser  = new ConcurrentHashMap<>();

    /**
     * Returns 0–100 risk score.
     */
    public int calculateRisk(String email, String ip, String userAgent) {
        long now = Instant.now().toEpochMilli();
        int score = 0;

        // ---------- 1. Device/IP change ----------
        LastLoginInfo last = lastLoginByUser.get(email);
        if (last != null) {
            if (!last.ip.equals(ip)) {
                score += 30; // new IP
            }
            if (!normalizeUa(last.userAgent).equals(normalizeUa(userAgent))) {
                score += 20; // new device / browser
            }

            long diffMin = (now - last.ts) / 60000;
            if (diffMin < 5) {
                score += 10; // very frequent logins
            }
        }

        // ---------- 2. Velocity / brute-force ----------
        List<Long> times = loginTimesByUser
                .computeIfAbsent(email, k -> new ArrayList<>());
        times.add(now);
        // keep only last 10 entries
        while (times.size() > 10) {
            times.remove(0);
        }

        long windowMs = 10 * 60_000L;
        long recentCount = times.stream()
                .filter(t -> now - t <= windowMs)
                .count();
        if (recentCount >= 5) {
            score += 30; // many attempts in 10 minutes
        } else if (recentCount >= 3) {
            score += 15;
        }

        // clamp
        if (score > 100) score = 100;

        log.debug("LoginRiskEngine score={} for email={} ip={} ua={}",
                score, email, ip, userAgent);

        // update last login
        lastLoginByUser.put(email, new LastLoginInfo(ip, userAgent, now));

        return score;
    }

    private String normalizeUa(String ua) {
        if (ua == null) return "";
        ua = ua.toLowerCase();
        if (ua.contains("chrome")) return "chrome";
        if (ua.contains("firefox")) return "firefox";
        if (ua.contains("safari")) return "safari";
        if (ua.contains("edge")) return "edge";
        return ua;
    }
}
