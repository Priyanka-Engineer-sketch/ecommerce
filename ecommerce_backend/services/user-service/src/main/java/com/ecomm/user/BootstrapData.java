package com.ecomm.user;

import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class BootstrapData implements CommandLineRunner {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            // --- Create roles if not exist ---
            Role adminRole = roleRepo.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepo.save(new Role(null, "ROLE_ADMIN")));
            Role userRole = roleRepo.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepo.save(new Role(null, "ROLE_USER")));

            // --- Admin ---
            User admin = User.builder()
                    .email("admin@example.com")
                    .username("admin")
                    .passwordHash(encoder.encode("Admin@12345"))
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(adminRole, userRole)) // give both roles
                    .build();
            userRepo.save(admin);

            // --- Normal User ---
            User user = User.builder()
                    .email("user@example.com")
                    .username("user")
                    .passwordHash(encoder.encode("User@12345"))
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(userRole))
                    .build();
            userRepo.save(user);
        }
    }
}
