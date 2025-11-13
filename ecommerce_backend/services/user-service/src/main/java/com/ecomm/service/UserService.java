package com.ecomm.service;

import com.ecomm.dto.request.ProfileUpdateRequest;
import com.ecomm.dto.response.MeResponse;
import com.ecomm.dto.response.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);

    UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);

    void deleteProfile(Long userId);

    MeResponse getCurrentUser();
}