package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import com.careflow.hospitalmanagement.dto.staff.*;
import com.careflow.hospitalmanagement.security.UserPrincipal;
import com.careflow.hospitalmanagement.service.StaffRegistrationService;
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
@Tag(name = "Staff Registration", description = "Staff Self-Service Onboarding and Administrative Review Endpoints")
public class StaffRegistrationController {

    private final StaffRegistrationService staffRegistrationService;

    public StaffRegistrationController(StaffRegistrationService staffRegistrationService) {
        this.staffRegistrationService = staffRegistrationService;
    }

    @PostMapping("/api/v1/staff-registration/requests")
    @Operation(summary = "Submit staff onboarding registration request for administrative review")
    public ResponseEntity<StaffRegistrationRequestDto> submitStaffRegistrationRequest(
            @Valid @RequestBody SubmitStaffRegistrationRequest request) {
        StaffRegistrationRequestDto created = staffRegistrationService.submitStaffRegistrationRequest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/v1/staff-registration/requests/{id}/status")
    @Operation(summary = "Check registration status by request ID")
    public ResponseEntity<?> getRegistrationStatusById(@PathVariable int id) {
        StaffRegistrationRequestDto status = staffRegistrationService.getStaffRegistrationRequestById(id);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No registration record found for request #" + id + "."));
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/api/v1/staff-registration/status")
    @Operation(summary = "Check registration status for a staff onboarding request by email")
    public ResponseEntity<?> getRegistrationStatus(@RequestParam String email) {
        StaffRegistrationRequestDto status = staffRegistrationService.getStaffRegistrationStatus(email);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No registration record found for email '" + email + "'."));
        }
        return ResponseEntity.ok(status);
    }

    @GetMapping("/api/v1/admin/staff-registration/requests")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "List staff registration requests for administrative review")
    public ResponseEntity<List<StaffRegistrationRequestDto>> getStaffRequests(
            @RequestParam(required = false) Integer status) {
        RegistrationStatus regStatus = status != null ? RegistrationStatus.fromValue(status) : null;
        List<StaffRegistrationRequestDto> requests = staffRegistrationService.getStaffRegistrationRequests(regStatus);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/api/v1/admin/staff-registration/requests/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Retrieve single staff registration request details for admin review")
    public ResponseEntity<?> getStaffRequestById(@PathVariable int id) {
        StaffRegistrationRequestDto request = staffRegistrationService.getStaffRegistrationRequestById(id);
        if (request == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Staff registration request #" + id + " not found."));
        }
        return ResponseEntity.ok(request);
    }

    @PostMapping("/api/v1/admin/staff-registration/requests/{id}/approve")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Approve a staff registration request and provision user account")
    public ResponseEntity<?> approveStaffRequest(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody ApproveStaffRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin) {
            if (request.getAuthorizedRoleId() != null && request.getAuthorizedRoleId() == 1) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Hospital Managers cannot grant Administrator role."));
            }
            StaffRegistrationRequestDto existing = staffRegistrationService.getStaffRegistrationRequestById(id);
            if (existing != null && (request.getAuthorizedRoleId() == null || request.getAuthorizedRoleId() == 0) &&
                    (existing.getRequestedRoleId() == 1 || "Admin".equalsIgnoreCase(existing.getRequestedRoleName()))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Hospital Managers cannot approve requests for Administrator accounts."));
            }
        }

        StaffRegistrationRequestDto approved = staffRegistrationService.approveStaffRegistrationRequest(id, principal.getUserId(), request);
        return ResponseEntity.ok(approved);
    }

    @PostMapping("/api/v1/admin/staff-registration/requests/{id}/reject")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Reject a staff registration request")
    public ResponseEntity<?> rejectStaffRequest(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RejectStaffRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StaffRegistrationRequestDto rejected = staffRegistrationService.rejectStaffRegistrationRequest(id, principal.getUserId(), request);
        return ResponseEntity.ok(rejected);
    }

    @PutMapping("/api/v1/admin/staff-registration/requests/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Update details and/or stage of a staff registration request")
    public ResponseEntity<?> updateStaffRequest(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody UpdateStaffRegistrationRequest request) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "Admin".equalsIgnoreCase(principal.getRoleName());
        if (!isAdmin && request.getRequestedRoleId() != null && request.getRequestedRoleId() == 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Hospital Managers cannot assign Administrator privileges."));
        }

        StaffRegistrationRequestDto updated = staffRegistrationService.updateStaffRegistrationRequest(id, principal.getUserId(), request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/api/v1/admin/staff-registration/requests/{id}/retry-email")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Retry sending notification email for a staff registration request")
    public ResponseEntity<StaffRegistrationRequestDto> retryStaffNotificationEmail(@PathVariable int id) {
        StaffRegistrationRequestDto retried = staffRegistrationService.retryStaffNotificationEmail(id);
        return ResponseEntity.ok(retried);
    }
}
