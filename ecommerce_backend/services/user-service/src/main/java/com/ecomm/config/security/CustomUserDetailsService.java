package com.ecomm.config.security;

import com.ecomm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userRepository.findAuthUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<GrantedAuthority> authorities =
                user.getRoles().stream()
                        .flatMap(role -> Stream.concat(
                                Stream.of(new SimpleGrantedAuthority(role.getName())), // e.g. ROLE_USER
                                role.getPermissions().stream()
                                        .map(p -> new SimpleGrantedAuthority("PERM_" + p.getName()))
                        ))
                        .collect(Collectors.toSet());

        return User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .disabled(!user.getIsActive())
                .authorities(authorities)
                .build();
    }
}
