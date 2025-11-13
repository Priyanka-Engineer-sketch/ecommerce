package com.ecomm.service;

import com.ecomm.dto.*;
import com.ecomm.dto.request.ChangePasswordRequest;
import com.ecomm.dto.request.SelfUpdateRequest;
import com.ecomm.dto.response.UserResponse;

public interface UserSelfService {
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(SelfUpdateRequest req);
    void changePassword(ChangePasswordRequest req);
    UserResponse patchStatus(boolean active);
}