package com.ecomm.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.Data;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple per-instance (in-memory) token-bucket rate limiter using Bucket4j.
 * Key: X-User-Id if present, else client IP. Suitable for POC/single-node.
 *
 * Filter name in application.yml:  "Bucket4jRateLimiter"
 */
@Component
public class Bucket4jRateLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Bucket4jRateLimiterGatewayFilterFactory.Config> {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket4jRateLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        Duration period = parse(config.refillPeriod);
        Bandwidth limit = Bandwidth.classic(config.capacity, Refill.greedy(config.refillTokens, period));

        return (exchange, chain) -> {
            String key = resolveKey(exchange);
            Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder().addLimit(limit).build());
            if (bucket.tryConsume(1)) {
                return chain.filter(exchange);
            }
            return tooMany(exchange);
        };
    }

    private static String resolveKey(ServerWebExchange exchange) {
        var h = exchange.getRequest().getHeaders();
        var user = h.getFirst("X-User-Id");
        if (user != null && !user.isBlank()) return "u:" + user;
        var addr = exchange.getRequest().getRemoteAddress();
        return "ip:" + (addr != null ? addr.getAddress().getHostAddress() : "anon");
    }

    private static Duration parse(String s) {
        if (s == null || s.isBlank()) return Duration.ofSeconds(1);
        s = s.trim().toLowerCase();
        if (s.endsWith("ms")) return Duration.ofMillis(Long.parseLong(s.replace("ms", "")));
        if (s.endsWith("s"))  return Duration.ofSeconds(Long.parseLong(s.replace("s", "")));
        if (s.endsWith("m"))  return Duration.ofMinutes(Long.parseLong(s.replace("m", "")));
        if (s.endsWith("h"))  return Duration.ofHours(Long.parseLong(s.replace("h", "")));
        return Duration.parse(s); // fallback ISO-8601
    }

    private static Mono<Void> tooMany(ServerWebExchange exchange) {
        var res = exchange.getResponse();
        res.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var body = "{\"error\":\"rate_limited\",\"message\":\"Too many requests\"}"
                .getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(body)));
    }

    @Data
    public static class Config {
        /** Max tokens in bucket (burst). */
        private long capacity = 50;
        /** Tokens added per period. */
        private long refillTokens = 50;
        /** Period: e.g., 1s, 500ms, 1m, 1h (or ISO-8601). */
        private String refillPeriod = "1s";
    }
}
