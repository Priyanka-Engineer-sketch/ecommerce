package com.ecomm.service.impl;


import com.ecomm.dto.request.AdminCreateUserRequest;
import com.ecomm.dto.request.AssignRolesRequest;
import com.ecomm.dto.request.StatusPatchRequest;
import com.ecomm.dto.request.UpdateUserRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.entity.Role;
import com.ecomm.entity.User;
import com.ecomm.repository.RoleRepository;
import com.ecomm.repository.UserRepository;
import com.ecomm.service.UserAdminService;
import com.ecomm.util.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @CacheEvict(value = {"users", "usersList"}, allEntries = true)
    // Clear caches after new user creation
    public UserResponse create(AdminCreateUserRequest req) {
        if (userRepo.existsByEmailIgnoreCase(req.getEmail()))
            throw new IllegalArgumentException("Email already in use");

        Set<Role> roles = resolveRolesOrDefault(req.getRoles());
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .isActive(req.getActive() == null ? true : req.getActive())
                .roles(roles)
                .build();

        return UserMapper.toResponse(userRepo.save(user));
    }

    @Override
    @CacheEvict(value = {"users", "usersList"}, allEntries = true)
    //  Clear caches after update
    public UserResponse update(Long id, UpdateUserRequest req) {
        User user = userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        if (req.getUsername() != null) user.setUsername(req.getUsername());
        if (req.getEmail() != null) {
            if (userRepo.existsByEmailIgnoreCase(req.getEmail()) && !req.getEmail().equalsIgnoreCase(user.getEmail()))
                throw new IllegalArgumentException("Email already in use");
            user.setEmail(req.getEmail());
        }
        if (req.getPassword() != null) user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getActive() != null) user.setIsActive(req.getActive());
        return UserMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = {"users", "usersList"}, allEntries = true)
    //  Clear caches when user deleted
    public void delete(Long id) {
        if (!userRepo.existsById(id)) throw new NoSuchElementException("User not found");
        userRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    // Cache user by ID
    public UserResponse get(Long id) {
        User user = userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "usersList", key = "{#query, #pageable.pageNumber, #pageable.pageSize}")
    public PageResponse<UserResponse> list(String query, Pageable pageable) {
        Page<User> page = (query == null || query.isBlank())
                ? userRepo.findAll(pageable)
                : userRepo.search(query.trim(), pageable);

        return PageResponse.<UserResponse>builder()
                .content(page.getContent().stream().map(UserMapper::toResponse).toList())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @CacheEvict(value = {"users", "usersList"}, allEntries = true)
    public UserResponse assignRoles(Long id, AssignRolesRequest req) {
        User user = userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        Set<Role> roles = resolveRoles(req.getRoles());
        user.setRoles(roles);
        return UserMapper.toResponse(user);
    }

    @Override
    @CacheEvict(value = {"users", "usersList"}, allEntries = true)
    public UserResponse patchStatus(Long id, StatusPatchRequest req) {
        User user = userRepo.findById(id).orElseThrow(() -> new NoSuchElementException("User not found"));
        user.setIsActive(req.getActive());
        return UserMapper.toResponse(user);
    }

    private Set<Role> resolveRolesOrDefault(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of(roleRepo.findByName("ROLE_USER").orElseGet(() ->
                    roleRepo.save(Role.builder().name("ROLE_USER").description("Default User").build())));
        }
        return resolveRoles(names);
    }

    private Set<Role> resolveRoles(Set<String> names) {
        return names.stream().map(n ->
                roleRepo.findByName(n).orElseGet(() ->
                        roleRepo.save(Role.builder().name(n).description("Created by admin").build()))
        ).collect(Collectors.toSet());
    }
}