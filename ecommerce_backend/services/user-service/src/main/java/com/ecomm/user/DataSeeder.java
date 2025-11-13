package com.ecomm.user;

import com.ecomm.entity.Role;
import com.ecomm.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final RoleRepository roleRepo;

    @Override
    public void run(ApplicationArguments args) {
        List<String> base = List.of("ROLE_USER","ROLE_ADMIN","ROLE_MANAGER");
        base.forEach(r ->
                roleRepo.findByName(r).orElseGet(() -> roleRepo.save(Role.builder().name(r).description("seed").build()))
        );
    }
}