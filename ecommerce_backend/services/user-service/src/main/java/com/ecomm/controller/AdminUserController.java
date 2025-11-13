package com.ecomm.controller;

import com.ecomm.dto.*;
import com.ecomm.dto.request.AdminCreateUserRequest;
import com.ecomm.dto.request.AssignRolesRequest;
import com.ecomm.dto.request.RegisterRequest;
import com.ecomm.dto.request.StatusPatchRequest;
import com.ecomm.dto.request.UpdateUserRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.service.AuthService;
import com.ecomm.service.UserAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserAdminService service;

    @PostMapping
    public UserResponse create(@Valid @RequestBody AdminCreateUserRequest req) { return service.create(req); }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) { return service.get(id); }

    @GetMapping
    public PageResponse<UserResponse> list(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {
        Sort s = Sort.by(sort.split(",")[0]).descending();
        if (sort.toLowerCase().endsWith(",asc")) s = Sort.by(sort.split(",")[0]).ascending();
        Pageable pageable = PageRequest.of(page, size, s);
        return service.list(query, pageable);
    }

    @PutMapping("/{id}/roles")
    public UserResponse assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest req) {
        return service.assignRoles(id, req);
    }

    @PatchMapping("/{id}/status")
    public UserResponse patchStatus(@PathVariable Long id, @Valid @RequestBody StatusPatchRequest req) {
        return service.patchStatus(id, req);
    }
}
