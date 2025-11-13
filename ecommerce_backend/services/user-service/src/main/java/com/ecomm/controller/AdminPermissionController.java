package com.ecomm.controller;

import com.ecomm.dto.request.AssignPermissionsRequest;
import com.ecomm.dto.request.CreatePermissionRequest;
import com.ecomm.dto.request.UpdatePermissionRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.PermissionResponse;
import com.ecomm.entity.Permission;
import com.ecomm.entity.Role;
import com.ecomm.repository.PermissionRepository;
import com.ecomm.repository.RoleRepository;
import com.ecomm.service.PermissionAdminService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_WRITE') or hasAuthority('ROLE_ADMIN') or hasAuthority('PERMISSION_WRITE')")
public class AdminPermissionController {

    private final PermissionAdminService permissionService;
    private final RoleRepository roleRepo;
    private final PermissionRepository permRepo;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissionResponse create(@Valid @RequestBody CreatePermissionRequest req) {
        return permissionService.create(req);
    }

    @PutMapping("/{id}")
    public PermissionResponse update(@PathVariable Long id,
                                     @Valid @RequestBody UpdatePermissionRequest req) {
        return permissionService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }

    @GetMapping("/{id}")
    public PermissionResponse get(@PathVariable Long id) {
        return permissionService.get(id);
    }

    @GetMapping
    public PageResponse<PermissionResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        String[] s = sort.split(",");
        Sort.Direction dir = (s.length > 1 && "desc".equalsIgnoreCase(s[1])) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, s[0]));
        return permissionService.list(q, pageable);
    }

    @PostMapping("/{roleName}/permissions")
    @Transactional
    public void assignPermissions(@PathVariable String roleName,
                                  @Valid @RequestBody AssignPermissionsRequest req) {
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Set<Permission> perms = req.getPermissionNames().stream().map(n ->
                permRepo.findByNameIgnoreCase(n).orElseThrow(() -> new IllegalArgumentException("Permission not found: " + n))
        ).collect(Collectors.toSet());

        role.getPermissions().addAll(perms);
        roleRepo.save(role);
    }

    @DeleteMapping("/{roleName}/permissions/{permName}")
    @Transactional
    public void removePermission(@PathVariable String roleName, @PathVariable String permName) {
        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        Permission p = permRepo.findByNameIgnoreCase(permName)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found"));
        role.getPermissions().remove(p);
        roleRepo.save(role);
    }
}
