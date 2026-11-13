package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.dto.auth.*;
import com.careflow.hospitalmanagement.security.UserPrincipal;
import com.careflow.hospitalmanagement.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and Account Management Endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user credentials and receive JWT access token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new system user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Obtain new JWT access token using refresh token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody(required = false) Map<String, Object> body) {
        String token = "";
        if (body != null) {
            if (body.get("refreshToken") != null) {
                token = body.get("refreshToken").toString();
            } else if (body.get("token") != null) {
                token = body.get("token").toString();
            }
        }
        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get details of the currently authenticated user")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDto user = authService.getCurrentUser(principal.getUserId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update profile settings of the currently authenticated user")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDto updated = authService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password for currently authenticated user")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        authService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    @GetMapping("/roles")
    @Operation(summary = "Get list of active system roles with hierarchy levels and permissions")
    public ResponseEntity<List<RoleDto>> getRoles(@AuthenticationPrincipal UserPrincipal principal) {
        boolean isAdmin = principal != null && "Admin".equalsIgnoreCase(principal.getRoleName());
        List<RoleDto> roles = authService.getRoles(isAdmin);
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset email with secure token")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("message", "If an active account is associated with this email address, password reset instructions have been dispatched."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using secure email token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password successfully reset. You may now sign in with your new credentials."));
    }
}
