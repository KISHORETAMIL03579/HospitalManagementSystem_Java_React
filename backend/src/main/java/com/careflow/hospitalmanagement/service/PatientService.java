package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Patient;
import com.careflow.hospitalmanagement.domain.entities.User;
import com.careflow.hospitalmanagement.dto.patient.CreatePatientRequest;
import com.careflow.hospitalmanagement.dto.patient.PatientDto;
import com.careflow.hospitalmanagement.dto.patient.PatientListResponse;
import com.careflow.hospitalmanagement.repository.PatientRepository;
import com.careflow.hospitalmanagement.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public PatientService(PatientRepository patientRepository, UserRepository userRepository) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PatientDto getById(int patientId) {
        return patientRepository.findByPatientIdAndIsActiveTrue(patientId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public PatientDto getPatientForUser(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";

        // Try exact email match
        Optional<Patient> patientOpt = patientRepository.findByEmailIgnoreCaseAndIsActiveTrue(email);
        if (patientOpt.isPresent()) {
            return mapToDto(patientOpt.get());
        }

        // Special case: seeded demo patient user
        if ("patient@careflow.com".equalsIgnoreCase(email)) {
            patientOpt = patientRepository.findByMedicalRecordNumberAndIsActiveTrue("MRN-2026-0001");
            if (patientOpt.isPresent()) {
                return mapToDto(patientOpt.get());
            }
        }

        // Match by full name
        if (user.getFullName() != null && !user.getFullName().isBlank()) {
            String nameLower = user.getFullName().trim().toLowerCase();
            for (Patient p : patientRepository.findAll()) {
                if (p.isActive()) {
                    String pName = (p.getFirstName() + " " + p.getLastName()).toLowerCase();
                    if (pName.equals(nameLower) || nameLower.contains(pName) || pName.contains(nameLower)) {
                        return mapToDto(p);
                    }
                }
            }
        }

        return null;
    }

    @Transactional(readOnly = true)
    public PatientListResponse getPatients(String search, int page, int pageSize) {
        int pageNumber = Math.max(1, page) - 1; // Spring Page is 0-indexed
        int limit = Math.min(Math.max(1, pageSize), 100);

        Pageable pageable = PageRequest.of(pageNumber, limit);
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        Page<Patient> paged = patientRepository.findPatientsPaged(cleanSearch, pageable);
        List<PatientDto> items = paged.getContent().stream().map(this::mapToDto).toList();

        return new PatientListResponse(items, (int) paged.getTotalElements(), page, limit);
    }

    public PatientDto createPatient(CreatePatientRequest request) {
        String mrn = generateNextMrn();

        Patient p = new Patient();
        p.setMedicalRecordNumber(mrn);
        p.setFirstName(request.getFirstName().trim());
        p.setLastName(request.getLastName().trim());
        p.setDateOfBirth(request.getDateOfBirth());
        p.setGender(request.getGender());
        p.setBloodGroup(request.getBloodGroup());
        p.setPhone(request.getPhone().trim());
        p.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        p.setAddress(request.getAddress() != null && !request.getAddress().isBlank() ? request.getAddress().trim() : null);
        p.setMedicalHistory(request.getMedicalHistory() != null && !request.getMedicalHistory().isBlank() ? request.getMedicalHistory().trim() : null);
        p.setEmergencyContactName(request.getEmergencyContactName() != null && !request.getEmergencyContactName().isBlank() ? request.getEmergencyContactName().trim() : null);
        p.setEmergencyContactPhone(request.getEmergencyContactPhone() != null && !request.getEmergencyContactPhone().isBlank() ? request.getEmergencyContactPhone().trim() : null);
        p.setActive(true);

        Patient saved = patientRepository.save(p);
        return mapToDto(saved);
    }

    public synchronized String generateNextMrn() {
        int year = LocalDate.now().getYear();
        String prefix = "MRN-" + year + "-";
        List<String> mrns = patientRepository.findMrnsStartingWith(prefix);

        int maxSeq = 0;
        for (String m : mrns) {
            String numStr = m.replace(prefix, "");
            try {
                int num = Integer.parseInt(numStr);
                if (num > maxSeq) {
                    maxSeq = num;
                }
            } catch (NumberFormatException ignored) {}
        }

        int nextSeq = maxSeq + 1;
        return String.format("%s%05d", prefix, nextSeq);
    }

    public PatientDto mapToDto(Patient p) {
        PatientDto dto = new PatientDto();
        dto.setPatientId(p.getPatientId());
        dto.setMedicalRecordNumber(p.getMedicalRecordNumber());
        dto.setFirstName(p.getFirstName());
        dto.setLastName(p.getLastName());
        dto.setFullName(p.getFullName());
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setGender(p.getGender());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setPhone(p.getPhone());
        dto.setEmail(p.getEmail());
        dto.setAddress(p.getAddress());
        dto.setMedicalHistory(p.getMedicalHistory());
        dto.setEmergencyContactName(p.getEmergencyContactName());
        dto.setEmergencyContactPhone(p.getEmergencyContactPhone());
        dto.setActive(p.isActive());
        dto.setCreatedAt(p.getCreatedAt());
        return dto;
    }
}
