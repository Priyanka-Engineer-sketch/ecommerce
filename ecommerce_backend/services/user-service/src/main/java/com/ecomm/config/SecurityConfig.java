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

    private final JwtAuthFilter jwtAuthFilter;   // from security-common
    private final RateLimitFilter rateLimitFilter; // local to user-service

    private static final String[] PUBLIC = {
            // Public User APIs
            "/api/public/**",

            // Auth APIs
            "/api/users/register",
            "/api/users/login",
            "/api/users/refresh",
            "/api/users/verify-email",
            "/api/users/resend-verification",

            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/verify-email",
            "/api/auth/resend-verification",

            // OAuth login (future)
            "/oauth2/**",
            "/login/oauth2/**",

            // Docs / Monitoring
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) ->
                                writeJson(res, HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()

                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/users/me", "/api/users/me/**").authenticated()

                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                // Apply filters in correct order
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(rateLimitFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeJson(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + message + "\"}"
        );
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
