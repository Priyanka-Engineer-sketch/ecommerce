package com.ecomm.service;

import com.ecomm.dto.request.AdminCreateUserRequest;
import com.ecomm.dto.request.AssignRolesRequest;
import com.ecomm.dto.request.StatusPatchRequest;
import com.ecomm.dto.request.UpdateUserRequest;
import com.ecomm.dto.response.PageResponse;
import com.ecomm.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserAdminService {
    UserResponse create(AdminCreateUserRequest req);
    UserResponse update(Long id, UpdateUserRequest req);
    void delete(Long id);
    UserResponse get(Long id);
    PageResponse<UserResponse> list(String query, Pageable pageable);
    UserResponse assignRoles(Long id, AssignRolesRequest req);
    UserResponse patchStatus(Long id, StatusPatchRequest req);
}
