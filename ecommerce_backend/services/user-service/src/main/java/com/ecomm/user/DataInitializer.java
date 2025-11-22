package com.ecomm.user;

import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.entity.UserProfile;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {

            if (userRepository.count() > 0) {
                return; // already seeded
            }

            // 1) Create roles
            Role adminRole   = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            Role userRole    = roleRepository.save(Role.builder().name("ROLE_USER").build());
            Role sellerRole  = roleRepository.save(Role.builder().name("ROLE_SELLER").build());
            Role managerRole = roleRepository.save(Role.builder().name("ROLE_MANAGER").build());

            String pwd = passwordEncoder.encode("Password123!");

            Instant now = Instant.now();

            // 2) Admins
            User admin1 = User.builder()
                    .username("admin1")
                    .email("admin1@ecomm.com")
                    .passwordHash(pwd)
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(adminRole))
                    .build();

            User admin2 = User.builder()
                    .username("admin2")
                    .email("admin2@ecomm.com")
                    .passwordHash(pwd)
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(adminRole, managerRole))
                    .build();

            // 3) Sellers
            User seller1 = User.builder()
                    .username("fashionhouse")
                    .email("seller1@ecomm.com")
                    .passwordHash(pwd)
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(sellerRole))
                    .build();

            User seller2 = User.builder()
                    .username("gadgetworld")
                    .email("seller2@ecomm.com")
                    .passwordHash(pwd)
                    .isActive(true)
                    .isEmailVerified(true)
                    .tokenVersion(1)
                    .roles(Set.of(sellerRole))
                    .build();

            User seller3 = User.builder()
                    .username("organicmart")
                    .email("seller3@ecomm.com")
                    .passwordHash(pwd)
                    .isActive(true)
                    .isEmailVerified(false)
                    .tokenVersion(1)
                    .roles(Set.of(sellerRole))
                    .build();

            // 4) Normal users / buyers
            User u1 = User.builder().username("rahul23")
                    .email("rahul23@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u2 = User.builder().username("priya_s")
                    .email("priya_s@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u3 = User.builder().username("vivek2024")
                    .email("vivek2024@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(false).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u4 = User.builder().username("muskan89")
                    .email("muskan89@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u5 = User.builder().username("john_doe")
                    .email("john.doe@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u6 = User.builder().username("anjali_m")
                    .email("anjali.m@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(false).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u7 = User.builder().username("techlover")
                    .email("techlover@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            User u8 = User.builder().username("foodiequeen")
                    .email("foodie.queen@example.com").passwordHash(pwd)
                    .isActive(true).isEmailVerified(true).tokenVersion(1)
                    .roles(Set.of(userRole)).build();

            // Save all users
            List<User> allUsers = userRepository.saveAll(
                    List.of(admin1, admin2, seller1, seller2, seller3,
                            u1, u2, u3, u4, u5, u6, u7, u8)
            );

            // 5) Attach profiles (simplified)
            allUsers.forEach(u -> {
                UserProfile profile = UserProfile.builder()
                        .user(u)
                        .displayName(switch (u.getUsername()) {
                            case "fashionhouse" -> "Fashion House Store";
                            case "gadgetworld"  -> "Gadget World Official";
                            case "organicmart"  -> "Organic Mart";
                            case "rahul23"      -> "Rahul K.";
                            case "priya_s"      -> "Priya S.";
                            default -> u.getUsername();
                        })
                        .avatarUrl("https://picsum.photos/seed/" + u.getUsername() + "/200/200")
                        .city("Bengaluru")
                        .rating(u.getRoles().contains(sellerRole) ? 4.5 : null)
                        .totalReviews(u.getRoles().contains(sellerRole) ? 120 : null)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                // Important: set the profile on user entity
                u.setProfile(profile);
            });

            userRepository.saveAll(allUsers);
        };
    }
}
