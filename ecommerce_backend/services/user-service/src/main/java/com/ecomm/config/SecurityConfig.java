package com.ecomm.config;

import com.ecomm.config.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;

    // PUBLIC only truly public endpoints (no token needed)
    private static final String[] PUBLIC = {
            // user auth
            "/api/users/register",
            "/api/users/login",
            "/api/users/refresh",
            "/api/users/verify-email",
            "/api/users/resend-verification",

            // auth-controller public endpoints
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",

            // OAuth2 callbacks
            "/oauth2/**",
            "/login/oauth2/**",

            // Docs & health/info
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized Access"))
                        .accessDeniedHandler((req, res, e) ->
                                writeJson(res, HttpServletResponse.SC_FORBIDDEN, "Access Denied"))
                )
                .authorizeHttpRequests(auth -> auth
                        // 1) Fully public
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()

                        // 2) Authenticated user endpoints
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()

                        // 3) Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // (optional) any other /api/users/** that is not /me can be admin-only:
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 4) everything else needs authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeJson(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        String body = String.format("{\"status\":%d,\"error\":\"%s\"}", status, message);
        res.getWriter().write(body);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}

