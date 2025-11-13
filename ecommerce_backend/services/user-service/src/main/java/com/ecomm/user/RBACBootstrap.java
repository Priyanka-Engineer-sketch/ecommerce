package com.ecomm.user;

import com.ecomm.entity.Permission;
import com.ecomm.entity.Role;
import com.ecomm.repository.PermissionRepository;
import com.ecomm.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class RBACBootstrap implements ApplicationRunner {

    private final RoleRepository roleRepo;
    private final PermissionRepository permissionRepo;

    @Override
    @Transactional // keep a session open while touching LAZY collections
    public void run(ApplicationArguments args) {
        seedRbac();
    }

    @Transactional // explicit is fine; keeps seeding atomic
    void seedRbac() {
        seed("ROLE_ADMIN", Set.of(
                "USER_READ", "USER_WRITE",
                "ROLE_READ", "ROLE_WRITE",
                "PERMISSION_READ", "PERMISSION_WRITE"
        ));
        seed("ROLE_USER", Set.of("USER_READ"));
    }

    private void seed(String roleName, Set<String> permNames) {
        // fetch Role with permissions eagerly to avoid LAZY init problems
        Role role = roleRepo.findByNameWithPermissions(roleName)
                .orElseGet(() -> roleRepo.save(
                        Role.builder().name(roleName).description("seeded").build()
                ));

        boolean changed = false;
        for (String rawName : permNames) {
            String pName = rawName.trim();

            Permission p = permissionRepo.findByNameIgnoreCase(pName)
                    .orElseGet(() -> permissionRepo.save(
                            Permission.builder().name(pName).description("Seeded").build()
                    ));

            // avoid duplicates (case-insensitive compare)
            boolean hasIt = role.getPermissions().stream()
                    .anyMatch(x -> x.getName().equalsIgnoreCase(pName));

            if (!hasIt) {
                role.addPermission(p);
                changed = true;
            }
        }

        if (changed) {
            roleRepo.save(role);
        }
    }
}
