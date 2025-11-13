package com.ecomm.controller;


import com.ecomm.dto.request.ChangePasswordRequest;
import com.ecomm.dto.request.SelfUpdateRequest;
import com.ecomm.dto.response.UserResponse;
import com.ecomm.service.impl.UserSelfServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserSelfController {

    private final UserSelfServiceImpl service;

    @GetMapping
    public UserResponse me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }
        // still use the service so behavior stays in one place
        return service.getCurrentUser();
    }

    @PutMapping
    public UserResponse update(@Valid @RequestBody SelfUpdateRequest req) {
        return service.updateCurrentUser(req);
    }

    @PatchMapping("/password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        service.changePassword(req);
    }

    @PatchMapping("/status")
    public UserResponse deactivateOrActivate(@RequestParam boolean active) {
        return service.patchStatus(active);
    }
}
