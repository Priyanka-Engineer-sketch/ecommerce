package com.ecomm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Map<String, Bandwidth> RULES = Map.of(
            "/api/auth/login",    Bandwidth.simple(10, Duration.ofMinutes(1)),
            "/api/auth/register", Bandwidth.simple(5,  Duration.ofMinutes(1)),
            "/api/auth/refresh",  Bandwidth.simple(20, Duration.ofMinutes(1)),
            "/api/users/me/password", Bandwidth.simple(5, Duration.ofMinutes(1))
    );

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true; // CORS preflight
        String p = req.getRequestURI();
        return RULES.keySet().stream().noneMatch(p::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ruleKey = RULES.keySet().stream().filter(path::startsWith).findFirst().orElse(path);
        String key = request.getRemoteAddr() + ":" + ruleKey;

        Bucket bucket = buckets.computeIfAbsent(
                key,
                k -> Bucket.builder().addLimit(RULES.get(ruleKey)).build()
        );

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
        }
    }
}
