package com.ecomm.service.impl;

import com.ecomm.dto.request.CreatePermissionRequest;
import com.ecomm.dto.request.UpdatePermissionRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.PermissionResponse;
import com.ecomm.entity.Permission;
import com.ecomm.entity.Role;
import com.ecomm.repository.PermissionRepository;
import com.ecomm.service.PermissionAdminService;
import com.ecomm.util.PermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class PermissionAdminServiceImpl implements PermissionAdminService {

    private final PermissionRepository permissionRepo;

    @Override
    @CacheEvict(value = {"permissions", "permissionsList"}, allEntries = true)
    public PermissionResponse create(CreatePermissionRequest req) {
        String name = req.getName().trim();
        if (permissionRepo.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Permission already exists");
        }
        Permission p = Permission.builder()
                .name(name)
                .description(req.getDescription())
                .build();
        return PermissionMapper.toResponse(permissionRepo.save(p));
    }

    @Override
    @CacheEvict(value = {"permissions", "permissionsList"}, allEntries = true)
    public PermissionResponse update(Long id, UpdatePermissionRequest req) {
        Permission p = permissionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission not found"));

        if (req.getName() != null && !req.getName().isBlank()) {
            String newName = req.getName().trim();
            permissionRepo.findByNameIgnoreCase(newName).ifPresent(existing -> {
                if (!existing.getId().equals(p.getId())) {
                    throw new IllegalArgumentException("Permission name already in use");
                }
            });
            p.setName(newName);
        }
        if (req.getDescription() != null) {
            p.setDescription(req.getDescription());
        }
        return PermissionMapper.toResponse(p);
    }

    @Override
    @CacheEvict(value = {"permissions", "permissionsList"}, allEntries = true)
    public void delete(Long id) {
        Permission p = permissionRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Permission not found"));

        // detach from roles first (avoid FK issues on join table)
        for (Role r : p.getRoles()) {
            r.getPermissions().remove(p);
        }
        p.getRoles().clear();

        permissionRepo.delete(p);
    }

    @Override @Transactional(readOnly = true)
    @Cacheable(value = "permissions", key = "#id")
    public PermissionResponse get(Long id) {
        return permissionRepo.findById(id)
                .map(PermissionMapper::toResponse)
                .orElseThrow(() -> new NoSuchElementException("Permission not found"));
    }

    @Override @Transactional(readOnly = true)
    @Cacheable(value = "permissionsList", key = "{#q, #pageable.pageNumber, #pageable.pageSize}")
    public PageResponse<PermissionResponse> list(String q, Pageable pageable) {
        Page<Permission> page = (q == null || q.isBlank())
                ? permissionRepo.findAll(pageable)
                : permissionRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                q.trim(), q.trim(), pageable);

        return PageResponse.<PermissionResponse>builder()
                .content(page.map(PermissionMapper::toResponse).getContent())
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}

