package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.dto.admin.*;
import com.careflow.hospitalmanagement.security.UserPrincipal;
import com.careflow.hospitalmanagement.service.AdminUserService;
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
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
@Tag(name = "Admin - Users", description = "Direct User Administration Endpoints")
public class AdminUsersController {

    private final AdminUserService adminUserService;

    public AdminUsersController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Retrieve all registered user accounts for direct administration")
    public ResponseEntity<List<AdminUserDto>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer roleId,
            @RequestParam(required = false) Boolean isActive) {
        List<AdminUserDto> users = adminUserService.getAllUsersForAdmin(search, roleId, isActive);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Admin directly creates a new user account")
    public ResponseEntity<?> createUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateUserByAdminRequest request) {
        boolean isAdmin = principal != null && "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin && request.getRoleId() != null && request.getRoleId() == 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Hospital Managers cannot create Administrator accounts."));
        }

        AdminUserDto user = adminUserService.createUserByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edit user account details")
    public ResponseEntity<?> updateUser(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateUserByAdminRequest request) {
        boolean isAdmin = principal != null && "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin) {
            if (request.getRoleId() == 1 || isTargetAdmin(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Hospital Managers cannot modify Administrator accounts or elevate users to Administrator."));
            }
        }

        AdminUserDto updated = adminUserService.updateUserByAdmin(id, request);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Change assigned user role directly")
    public ResponseEntity<?> updateUserRole(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateUserRoleRequest request) {
        boolean isAdmin = principal != null && "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin) {
            if (request.getRoleId() == 1 || isTargetAdmin(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Hospital Managers cannot assign the Administrator role or modify Administrator accounts."));
            }
        }

        AdminUserDto updated = adminUserService.updateUserRoleByAdmin(id, request.getRoleId());
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle user account active status (Activate / Deactivate)")
    public ResponseEntity<?> toggleUserStatus(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateUserStatusRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin && isTargetAdmin(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Hospital Managers cannot change the status of Administrator accounts."));
        }

        AdminUserDto updated = adminUserService.toggleUserStatusByAdmin(id, principal.getUserId(), request.isActive());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete / remove user account")
    public ResponseEntity<?> deleteUser(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin && isTargetAdmin(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Hospital Managers cannot remove Administrator accounts."));
        }

        adminUserService.deleteUserByAdmin(id, principal.getUserId());
        return ResponseEntity.ok(Map.of("message", "User account deactivated and archived successfully. Medical records preserved."));
    }

    private boolean isTargetAdmin(int targetUserId) {
        List<AdminUserDto> users = adminUserService.getAllUsersForAdmin(null, null, null);
        return users.stream()
                .anyMatch(u -> u.getUserId() == targetUserId && (u.getRoleId() == 1 || "Admin".equalsIgnoreCase(u.getRoleName())));
    }
}
