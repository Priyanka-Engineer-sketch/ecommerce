package com.ecomm.service;

import com.ecomm.dto.request.CreatePermissionRequest;
import com.ecomm.dto.request.UpdatePermissionRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.PermissionResponse;
import org.springframework.data.domain.Pageable;

public interface PermissionAdminService {
    PermissionResponse create(CreatePermissionRequest req);
    PermissionResponse update(Long id, UpdatePermissionRequest req);
    void delete(Long id);
    PermissionResponse get(Long id);
    PageResponse<PermissionResponse> list(String query, Pageable pageable);
}
