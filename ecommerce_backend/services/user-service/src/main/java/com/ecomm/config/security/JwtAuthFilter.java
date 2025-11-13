package com.ecomm.config.security;

import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);

        // ---- No token? Let it pass to next filters (like public endpoints)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7);

        // ---- Validate JWT signature/expiry
        if (!jwtService.validate(token)) {
            log.debug("Invalid JWT: {}", token);
            SecurityContextHolder.clearContext();
            chain.doFilter(req, res);
            return;
        }

        String email = jwtService.extractUsername(token);
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                try {
                    Integer version = jwtService.extractTokenVersion(token);
                    if (version != null && !version.equals(user.getTokenVersion())) {
                        log.debug("Token version mismatch for {}", email);
                        return;
                    }

                    Collection<GrantedAuthority> authorities = user.getRoles().stream()
                            .map(Role::getName)
                            .map(name -> name.startsWith("ROLE_") ? name : "ROLE_" + name)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    log.debug("Authenticated user: {} with roles {}", email, authorities);

                } catch (Exception ex) {
                    log.error("Error during JWT authentication for {}: {}", email, ex.getMessage());
                    SecurityContextHolder.clearContext();
                }
            });
        }

        chain.doFilter(req, res);
    }
}
