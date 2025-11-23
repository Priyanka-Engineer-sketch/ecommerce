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
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .authorizeExchange(ex -> ex
                        // allow CORS preflights
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()

                        // public paths (gateway only handles auth at filter level)
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/fallback/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()

                        // Gateway will enforce RBAC through JwtAuth filter
                        .anyExchange().permitAll()
                )
                .build();
    }
}
