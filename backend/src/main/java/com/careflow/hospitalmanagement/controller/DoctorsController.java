package com.careflow.hospitalmanagement.controller;

import com.careflow.hospitalmanagement.dto.doctor.*;
import com.careflow.hospitalmanagement.service.DoctorAvailabilityService;
import com.careflow.hospitalmanagement.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctors", description = "Doctor Management, Scheduling, Availability & Exception Endpoints")
public class DoctorsController {

    private final DoctorService doctorService;
    private final DoctorAvailabilityService availabilityService;

    public DoctorsController(DoctorService doctorService, DoctorAvailabilityService availabilityService) {
        this.doctorService = doctorService;
        this.availabilityService = availabilityService;
    }

    @GetMapping
    @Operation(summary = "Get all doctors with optional department filter")
    public ResponseEntity<List<DoctorDto>> getDoctors(@RequestParam(required = false) Integer departmentId) {
        List<DoctorDto> doctors = doctorService.getDoctors(departmentId);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor details by Doctor ID")
    public ResponseEntity<?> getDoctorById(@PathVariable int id) {
        DoctorDto doctor = doctorService.getDoctorById(id);
        if (doctor == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Doctor with ID '" + id + "' was not found."));
        }
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/departments")
    @Operation(summary = "Get all active hospital departments")
    public ResponseEntity<List<DepartmentDto>> getDepartments() {
        List<DepartmentDto> departments = doctorService.getDepartments();
        return ResponseEntity.ok(departments);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager')")
    @Operation(summary = "Register a new doctor")
    public ResponseEntity<DoctorDto> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorDto created = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/directory")
    @Operation(summary = "Get doctor directory with live dynamic availability status")
    public ResponseEntity<List<DoctorDirectoryItemDto>> getDoctorDirectory(
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<DoctorDirectoryItemDto> directory = availabilityService.getDoctorDirectory(departmentId, search, date);
        return ResponseEntity.ok(directory);
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Get detailed availability and time slots for a doctor on a specific date")
    public ResponseEntity<DoctorAvailabilityDto> getDoctorAvailability(
            @PathVariable int id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        DoctorAvailabilityDto availability = availabilityService.getDoctorAvailability(id, targetDate);
        return ResponseEntity.ok(availability);
    }

    @GetMapping("/{id}/schedules")
    @Operation(summary = "Get weekly recurring schedules for a doctor")
    public ResponseEntity<List<DoctorScheduleDto>> getDoctorSchedules(@PathVariable int id) {
        List<DoctorScheduleDto> schedules = availabilityService.getDoctorSchedules(id);
        return ResponseEntity.ok(schedules);
    }

    @PutMapping("/{id}/schedules")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Receptionist')")
    @Operation(summary = "Save or replace recurring schedules for a doctor")
    public ResponseEntity<List<DoctorScheduleDto>> saveDoctorSchedules(
            @PathVariable int id,
            @RequestBody List<SaveDoctorScheduleDto> schedules) {
        List<DoctorScheduleDto> result = availabilityService.saveDoctorSchedules(id, schedules);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}/schedule-exceptions")
    @Operation(summary = "Get date-specific schedule exceptions")
    public ResponseEntity<List<DoctorScheduleExceptionDto>> getDoctorExceptions(
            @PathVariable int id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        List<DoctorScheduleExceptionDto> exceptions = availabilityService.getDoctorExceptions(id, fromDate, toDate);
        return ResponseEntity.ok(exceptions);
    }

    @PostMapping({"/{id}/schedule-exceptions/check-conflicts", "/{id}/check-conflicts"})
    @Operation(summary = "Check for conflicts between proposed schedule exception and existing patient appointments")
    public ResponseEntity<ConflictCheckResultDto> checkConflicts(
            @PathVariable int id,
            @RequestBody CreateDoctorScheduleExceptionDto dto) {
        ConflictCheckResultDto result = availabilityService.checkExceptionConflicts(id, dto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/schedule-exceptions")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Receptionist')")
    @Operation(summary = "Create a date-specific schedule exception")
    public ResponseEntity<DoctorScheduleExceptionDto> createDoctorException(
            @PathVariable int id,
            @Valid @RequestBody CreateDoctorScheduleExceptionDto dto) {
        DoctorScheduleExceptionDto created = availabilityService.createException(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}/schedule-exceptions/{exceptionId}")
    @PreAuthorize("hasAnyRole('Admin', 'HospitalManager', 'Doctor', 'Receptionist')")
    @Operation(summary = "Delete a schedule exception")
    public ResponseEntity<Void> deleteDoctorException(
            @PathVariable int id,
            @PathVariable int exceptionId) {
        availabilityService.deleteException(id, exceptionId);
        return ResponseEntity.noContent().build();
    }
}
