package com.ecomm.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // 🔴 IMPORTANT: disable CSRF for stateless API calls (Postman, SPA, etc.)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(ex -> ex
                        // allow CORS preflight
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // public auth endpoints through gateway
                        .pathMatchers("/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/refresh",
                                "/actuator/**")
                        .permitAll()

                        // everything else (for now) – you can tighten later
                        .anyExchange().permitAll()
                )
                .build();
    }
}
