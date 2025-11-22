package com.ecomm.apigateway.filter;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSAlgorithmFamilyJWSKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component("JwtAuth") // name must match the YAML filter name
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    // Explicit Log4j2 logger (replaces Lombok @Log4j2)
    private static final Logger log = LogManager.getLogger(JwtAuthGatewayFilterFactory.class);

    @Value("${security.jwt.jwks-uri:}")
    private String jwksUri;

    @Value("${security.jwt.issuer:}")
    private String expectedIssuer;

    @Value("${security.jwt.audience:}")
    private String expectedAudience;

    // For HMAC (HS256/384/512). Keep it long enough (>= 32 bytes).
    @Value("${security.jwt.hmac-secret:change-me-min-32-bytes-secret}")
    private String hmacSecret;

    // Lazily built processors for RSA and EC families
    private volatile ConfigurableJWTProcessor<SecurityContext> rsaProcessor;
    private volatile ConfigurableJWTProcessor<SecurityContext> ecProcessor;

    public JwtAuthGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (auth == null || !auth.startsWith("Bearer ")) {
                return config.required ? unauthorized(exchange, "Missing Bearer token") : chain.filter(exchange);
            }

            String token = auth.substring(7);

            try {
                SignedJWT signed = SignedJWT.parse(token);
                JWSAlgorithm alg = signed.getHeader().getAlgorithm();

                JWTClaimsSet claims;
                if (alg.getName().startsWith("HS")) {
                    // HMAC (symmetric)
                    claims = verifyHmac(signed);
                } else if (alg.getName().startsWith("RS") || alg.getName().startsWith("PS")) {
                    // RSA or RSASSA-PSS via JWKS
                    claims = verifyWithJwks(token, KeyFamily.RSA);
                } else if (alg.getName().startsWith("ES")) {
                    // ECDSA via JWKS
                    claims = verifyWithJwks(token, KeyFamily.EC);
                } else {
                    return unauthorized(exchange, "Unsupported JWS alg: " + alg);
                }

                // exp
                if (claims.getExpirationTime() == null || claims.getExpirationTime().toInstant().isBefore(Instant.now())) {
                    return unauthorized(exchange, "Token expired");
                }

                // iss / aud (optional)
                if (notBlank(expectedIssuer) && !expectedIssuer.equals(claims.getIssuer())) {
                    return unauthorized(exchange, "Invalid issuer");
                }
                if (notBlank(expectedAudience)) {
                    List<String> aud = claims.getAudience();
                    if (aud == null || aud.stream().noneMatch(expectedAudience::equals)) {
                        return unauthorized(exchange, "Invalid audience");
                    }
                }

                // Roles can be an array or a comma string; normalize
                List<String> roles = readRoles(claims.getClaim("roles"));

                // Optional route role check
                if (config.roles != null && !config.roles.isEmpty()) {
                    boolean ok = roles != null && roles.stream().anyMatch(config.roles::contains);
                    if (!ok) return unauthorized(exchange, "Insufficient role");
                }

                // Propagate identity
                String subject = Optional.ofNullable(claims.getSubject()).orElse("");
                var mutatedReq = exchange.getRequest().mutate()
                        .headers(h -> {
                            h.set("X-User-Id", subject);
                            h.set("X-User-Roles", roles != null ? String.join(",", roles) : "");
                        })
                        .build();

                return chain.filter(exchange.mutate().request(mutatedReq).build());

            } catch (java.text.ParseException e) {
                log.debug("JWT parse error", e);
                return unauthorized(exchange, "Malformed token");
            } catch (Exception e) {
                log.error("JWT error", e);
                return unauthorized(exchange, "Auth error");
            }
        };
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    @SuppressWarnings("unchecked")
    private static List<String> readRoles(Object claim) {
        if (claim == null) return null;
        if (claim instanceof List<?>) {
            return (List<String>) claim;
        }
        if (claim instanceof String s) {
            return List.of(s.split(","));
        }
        return null;
    }

    // ---------- HMAC verification ----------
    private JWTClaimsSet verifyHmac(SignedJWT signed) throws Exception {
        var verifier = new MACVerifier(hmacSecret.getBytes(StandardCharsets.UTF_8));
        if (!signed.verify(verifier)) throw new IllegalStateException("Invalid signature");
        return signed.getJWTClaimsSet();
    }

    // ---------- JWKS verification ----------
    private enum KeyFamily {RSA, EC}

    private JWTClaimsSet verifyWithJwks(String token, KeyFamily family) throws Exception {
        if (!notBlank(jwksUri)) throw new IllegalStateException("JWKS URI not configured");
        var processor = (family == KeyFamily.RSA) ? getOrBuildRsaProcessor() : getOrBuildEcProcessor();
        return processor.process(token, null);
    }

    private synchronized ConfigurableJWTProcessor<SecurityContext> getOrBuildRsaProcessor()
            throws MalformedURLException {
        if (rsaProcessor == null) {
            JWKSource<SecurityContext> source = new RemoteJWKSet<>(new URL(jwksUri));
            var p = new DefaultJWTProcessor<SecurityContext>();
            JWSKeySelector<SecurityContext> selector =
                    new JWSAlgorithmFamilyJWSKeySelector<>(JWSAlgorithm.Family.RSA, source);
            p.setJWSKeySelector(selector);
            rsaProcessor = p;
        }
        return rsaProcessor;
    }

    private synchronized ConfigurableJWTProcessor<SecurityContext> getOrBuildEcProcessor()
            throws MalformedURLException {
        if (ecProcessor == null) {
            JWKSource<SecurityContext> source = new RemoteJWKSet<>(new URL(jwksUri));
            var p = new DefaultJWTProcessor<SecurityContext>();
            JWSKeySelector<SecurityContext> selector =
                    new JWSAlgorithmFamilyJWSKeySelector<>(JWSAlgorithm.Family.EC, source);
            p.setJWSKeySelector(selector);
            ecProcessor = p;
        }
        return ecProcessor;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        var res = exchange.getResponse();
        res.setStatusCode(HttpStatus.UNAUTHORIZED);
        res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var bytes = ("{\"error\":\"unauthorized\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return res.writeWith(Mono.just(res.bufferFactory().wrap(bytes)));
    }

    @Data
    public static class Config {
        private boolean required = true;
        private List<String> roles; // require any of these roles (optional)
    }
}
