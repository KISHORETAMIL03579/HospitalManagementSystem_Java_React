package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Appointment;
import com.careflow.hospitalmanagement.domain.entities.Doctor;
import com.careflow.hospitalmanagement.domain.entities.Patient;
import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import com.careflow.hospitalmanagement.dto.appointment.AppointmentDto;
import com.careflow.hospitalmanagement.dto.appointment.CreateAppointmentRequest;
import com.careflow.hospitalmanagement.dto.patient.PatientDto;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.AppointmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import com.careflow.hospitalmanagement.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            PatientService patientService) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointments(Integer patientId, Integer doctorId, LocalDate date) {
        List<Appointment> list = appointmentRepository.findFiltered(patientId, doctorId, date);
        List<AppointmentDto> dtos = new ArrayList<>();
        for (Appointment a : list) {
            dtos.add(mapToDto(a));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public List<AppointmentDto> getAppointmentsForUser(int userId) {
        PatientDto patient = patientService.getPatientForUser(userId);
        if (patient == null) {
            return Collections.emptyList();
        }
        return getAppointments(patient.getPatientId(), null, null);
    }

    @Transactional(readOnly = true)
    public Integer getPatientIdForUser(int userId) {
        PatientDto patient = patientService.getPatientForUser(userId);
        return patient != null ? patient.getPatientId() : null;
    }

    @Transactional(readOnly = true)
    public AppointmentDto getAppointmentById(int appointmentId) {
        return appointmentRepository.findByIdWithDetails(appointmentId)
                .map(this::mapToDto)
                .orElse(null);
    }

    public AppointmentDto createAppointment(CreateAppointmentRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + request.getPatientId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with ID: " + request.getDoctorId()));

        if (request.getAppointmentDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Appointment date cannot be in the past.");
        }

        LocalTime timeSlot;
        try {
            timeSlot = LocalTime.parse(request.getTimeSlot().trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Time slot must be a valid time (e.g. '09:00' or '14:30').");
        }

        if (appointmentRepository.isSlotBooked(request.getDoctorId(), request.getAppointmentDate(), timeSlot)) {
            throw new IllegalStateException("The requested doctor is already booked at " + request.getTimeSlot() +
                    " on " + request.getAppointmentDate() + ".");
        }

        Appointment appt = new Appointment();
        appt.setPatientId(patient.getPatientId());
        appt.setPatient(patient);
        appt.setDoctorId(doctor.getDoctorId());
        appt.setDoctor(doctor);
        appt.setAppointmentDate(request.getAppointmentDate());
        appt.setTimeSlot(timeSlot);
        appt.setStatus(AppointmentStatus.Confirmed);
        appt.setReason(request.getReason().trim());
        appt.setNotes(request.getNotes() != null && !request.getNotes().isBlank() ? request.getNotes().trim() : null);
        appt.setActive(true);

        Appointment saved = appointmentRepository.save(appt);
        return mapToDto(saved);
    }

    public void updateStatus(int appointmentId, AppointmentStatus status, String notes) {
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment with ID " + appointmentId + " was not found."));

        if (notes != null && !notes.isBlank()) {
            appt.setNotes(notes.trim());
        }

        // Apply domain state transitions
        switch (status) {
            case Confirmed -> appt.confirm();
            case CheckedIn -> appt.checkIn();
            case InConsultation -> appt.startConsultation();
            case Completed -> appt.complete();
            case Cancelled -> appt.cancel();
            case NoShow -> appt.markNoShow();
            default -> appt.setStatus(status);
        }

        appointmentRepository.save(appt);
    }

    public AppointmentDto mapToDto(Appointment a) {
        AppointmentDto dto = new AppointmentDto();
        dto.setAppointmentId(a.getAppointmentId());
        dto.setPatientId(a.getPatientId());
        dto.setPatientName(a.getPatient() != null ? a.getPatient().getFullName() : "Unknown Patient");
        dto.setMedicalRecordNumber(a.getPatient() != null ? a.getPatient().getMedicalRecordNumber() : "N/A");
        dto.setDoctorId(a.getDoctorId());
        dto.setDoctorName(a.getDoctor() != null ? a.getDoctor().getFullName() : "Unknown Doctor");
        dto.setSpecialization(a.getDoctor() != null ? a.getDoctor().getSpecialization() : "General");
        dto.setAppointmentDate(a.getAppointmentDate());
        dto.setTimeSlot(a.getTimeSlot() != null ? a.getTimeSlot().toString() : "09:00");
        dto.setStatus(a.getStatus());
        dto.setStatusName(a.getStatus() != null ? a.getStatus().name() : "Confirmed");
        dto.setReason(a.getReason());
        dto.setNotes(a.getNotes());
        dto.setCreatedAt(a.getCreatedAt());
        return dto;
    }
}
