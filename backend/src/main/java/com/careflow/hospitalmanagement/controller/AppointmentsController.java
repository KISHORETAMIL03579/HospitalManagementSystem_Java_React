package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import com.careflow.hospitalmanagement.dto.appointment.AppointmentDto;
import com.careflow.hospitalmanagement.dto.appointment.CreateAppointmentRequest;
import com.careflow.hospitalmanagement.dto.appointment.UpdateAppointmentStatusRequest;
import com.careflow.hospitalmanagement.security.UserPrincipal;
import com.careflow.hospitalmanagement.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Appointment Scheduling, Conflict Checking and Status Management Endpoints")
public class AppointmentsController {

    private final AppointmentService appointmentService;

    private static final Set<String> STAFF_ROLES = Set.of(
            "admin", "hospitalmanager", "doctor", "nurse", "receptionist", "pharmacist", "labtechnician"
    );

    public AppointmentsController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get list of appointments with optional filters")
    public ResponseEntity<List<AppointmentDto>> getAppointments(
            @RequestParam(required = false) Integer patientId,
            @RequestParam(required = false) Integer doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isStaff = STAFF_ROLES.contains(principal.getRoleName().toLowerCase().replace(" ", ""));
        if (isStaff) {
            List<AppointmentDto> appointments = appointmentService.getAppointments(patientId, doctorId, date);
            return ResponseEntity.ok(appointments);
        }

        // Patient: retrieve their own appointments based on userId
        List<AppointmentDto> myAppointments = appointmentService.getAppointmentsForUser(principal.getUserId());
        return ResponseEntity.ok(myAppointments);
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointments scoped to the authenticated patient/user")
    public ResponseEntity<List<AppointmentDto>> getMyAppointments(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<AppointmentDto> myAppointments = appointmentService.getAppointmentsForUser(principal.getUserId());
        return ResponseEntity.ok(myAppointments);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<?> getAppointmentById(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppointmentDto appt = appointmentService.getAppointmentById(id);
        if (appt == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Appointment with ID '" + id + "' was not found."));
        }

        boolean isStaff = STAFF_ROLES.contains(principal.getRoleName().toLowerCase().replace(" ", ""));
        if (!isStaff) {
            Integer resolvedPatientId = appointmentService.getPatientIdForUser(principal.getUserId());
            if (resolvedPatientId == null || appt.getPatientId() != resolvedPatientId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You are not authorized to view another patient's appointment details."));
            }
        }

        return ResponseEntity.ok(appt);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Book a new appointment")
    public ResponseEntity<AppointmentDto> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            boolean isPatientRole = "Patient".equalsIgnoreCase(principal.getRoleName());
            if (isPatientRole || request.getPatientId() <= 0) {
                Integer resolvedId = appointmentService.getPatientIdForUser(principal.getUserId());
                if (resolvedId != null && resolvedId > 0) {
                    request.setPatientId(resolvedId);
                }
            }
        }

        AppointmentDto created = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Nurse', 'Receptionist', 'Patient')")
    @Operation(summary = "Update appointment status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable int id,
            @Valid @RequestBody UpdateAppointmentStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AppointmentDto existing = appointmentService.getAppointmentById(id);
        if (existing == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Appointment with ID '" + id + "' was not found."));
        }

        boolean isStaff = STAFF_ROLES.contains(principal.getRoleName().toLowerCase().replace(" ", ""));
        if (!isStaff) {
            Integer resolvedPatientId = appointmentService.getPatientIdForUser(principal.getUserId());
            if (resolvedPatientId == null || existing.getPatientId() != resolvedPatientId) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You are not authorized to update another patient's appointment."));
            }
            if (request.getStatus() != AppointmentStatus.Cancelled) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Patients are only authorized to cancel their appointments."));
            }
        }

        appointmentService.updateStatus(id, request.getStatus(), request.getNotes());
        return ResponseEntity.noContent().build();
    }
}
