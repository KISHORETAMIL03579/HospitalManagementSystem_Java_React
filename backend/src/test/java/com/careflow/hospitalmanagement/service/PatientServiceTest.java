package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Patient;
import com.careflow.hospitalmanagement.domain.enums.BloodGroup;
import com.careflow.hospitalmanagement.domain.enums.Gender;
import com.careflow.hospitalmanagement.dto.patient.CreatePatientRequest;
import com.careflow.hospitalmanagement.dto.patient.PatientDto;
import com.careflow.hospitalmanagement.repository.PatientRepository;
import com.careflow.hospitalmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = new Patient();
        samplePatient.setPatientId(1);
        samplePatient.setFirstName("John");
        samplePatient.setLastName("Smith");
        samplePatient.setEmail("john.smith@example.com");
        samplePatient.setMedicalRecordNumber("MRN-2026-0001");
        samplePatient.setDateOfBirth(LocalDate.of(1985, 5, 20));
        samplePatient.setGender(Gender.Male);
        samplePatient.setBloodGroup(BloodGroup.OPositive);
        samplePatient.setActive(true);
    }

    @Test
    @DisplayName("getById returns DTO when found")
    void testGetById() {
        when(patientRepository.findByPatientIdAndIsActiveTrue(1)).thenReturn(Optional.of(samplePatient));

        PatientDto result = patientService.getById(1);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getMedicalRecordNumber()).isEqualTo("MRN-2026-0001");
    }

    @Test
    @DisplayName("createPatient generates MRN and saves patient")
    void testCreatePatient() {
        CreatePatientRequest req = new CreatePatientRequest();
        req.setFirstName("Alice");
        req.setLastName("Brown");
        req.setDateOfBirth(LocalDate.of(1990, 1, 1));
        req.setGender(Gender.Female);
        req.setPhone("+1234567890");
        req.setEmail("alice@example.com");
        req.setAddress("123 Main St");
        req.setBloodGroup(BloodGroup.APositive);
        req.setEmergencyContactName("Bob");
        req.setEmergencyContactPhone("+1234567891");

        when(patientRepository.findMrnsStartingWith(any())).thenReturn(Collections.emptyList());
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> {
            Patient p = invocation.getArgument(0);
            p.setPatientId(6);
            return p;
        });

        PatientDto result = patientService.createPatient(req);

        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Alice");
        assertThat(result.getMedicalRecordNumber()).contains("MRN-");
        verify(patientRepository).save(any(Patient.class));
    }
}
