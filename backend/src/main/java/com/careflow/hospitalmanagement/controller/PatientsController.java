package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.dto.patient.CreatePatientRequest;
import com.careflow.hospitalmanagement.dto.patient.PatientDto;
import com.careflow.hospitalmanagement.dto.patient.PatientListResponse;
import com.careflow.hospitalmanagement.security.UserPrincipal;
import com.careflow.hospitalmanagement.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Patient Directory and Registration Endpoints")
public class PatientsController {

    private final PatientService patientService;

    private static final Set<String> STAFF_ROLES = Set.of(
            "admin", "hospitalmanager", "doctor", "nurse", "receptionist", "pharmacist", "labtechnician"
    );

    public PatientsController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Nurse', 'Receptionist', 'Pharmacist', 'LabTechnician')")
    @Operation(summary = "Get paginated list of patients with optional search query (hospital staff only)")
    public ResponseEntity<PatientListResponse> getPatients(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PatientListResponse response = patientService.getPatients(search, page, pageSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated patient's record")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PatientDto patient = patientService.getPatientForUser(principal.getUserId());
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Patient record not found for authenticated user."));
        }
        return ResponseEntity.ok(patient);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get patient details by Patient ID")
    public ResponseEntity<?> getPatientById(
            @PathVariable int id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isStaff = STAFF_ROLES.contains(principal.getRoleName().toLowerCase().replace(" ", ""));
        if (!isStaff) {
            PatientDto myPatient = patientService.getPatientForUser(principal.getUserId());
            if (myPatient == null || myPatient.getPatientId() != id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "You are not authorized to view another patient's medical records."));
            }
        }

        PatientDto patient = patientService.getById(id);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Patient with ID '" + id + "' was not found."));
        }
        return ResponseEntity.ok(patient);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Nurse', 'Receptionist')")
    @Operation(summary = "Register a new patient")
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        PatientDto created = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
