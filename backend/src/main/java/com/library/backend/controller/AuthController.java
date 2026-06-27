package com.library.backend.controller;

import com.library.backend.dto.AuthResponse;
import com.library.backend.dto.ChangePasswordRequest;
import com.library.backend.dto.LoginRequest;
import com.library.backend.dto.ProfileUpdateRequest;
import com.library.backend.security.AuthUser;
import com.library.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public AuthResponse me(Authentication authentication) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return authService.me(user);
    }

    @PostMapping("/change-password")
    public AuthResponse changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return authService.changePassword(user, request);
    }

    @PutMapping("/profile")
    public AuthResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        AuthUser user = (AuthUser) authentication.getPrincipal();
        return authService.updateProfile(user, request);
    }
}
