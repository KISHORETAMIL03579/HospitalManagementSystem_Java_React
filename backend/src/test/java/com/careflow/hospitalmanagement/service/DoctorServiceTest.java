package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Department;
import com.careflow.hospitalmanagement.domain.entities.Doctor;
import com.careflow.hospitalmanagement.domain.entities.User;
import com.careflow.hospitalmanagement.dto.doctor.DepartmentDto;
import com.careflow.hospitalmanagement.dto.doctor.DoctorDto;
import com.careflow.hospitalmanagement.repository.DepartmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor sampleDoctor;
    private Department sampleDept;

    @BeforeEach
    void setUp() {
        sampleDept = new Department();
        sampleDept.setDepartmentId(1);
        sampleDept.setName("Cardiology");
        sampleDept.setCode("CARD");

        sampleDoctor = new Doctor();
        sampleDoctor.setDoctorId(1);
        sampleDoctor.setDepartmentId(1);
        sampleDoctor.setDepartment(sampleDept);
        sampleDoctor.setFirstName("Sarah");
        sampleDoctor.setLastName("Connor");
        sampleDoctor.setSpecialization("Interventional Cardiology");
        sampleDoctor.setConsultationFee(new BigDecimal("150.00"));

        User u = new User();
        u.setFullName("Sarah Connor");
        u.setEmail("sarah@careflow.com");
        sampleDoctor.setUser(u);
    }

    @Test
    @DisplayName("getDoctors returns active doctors")
    void testGetDoctors() {
        when(doctorRepository.findAllActiveWithDepartment(null)).thenReturn(List.of(sampleDoctor));

        List<DoctorDto> result = doctorService.getDoctors(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDepartmentName()).isEqualTo("Cardiology");
        assertThat(result.get(0).getSpecialization()).isEqualTo("Interventional Cardiology");
    }

    @Test
    @DisplayName("getDoctorById returns doctor dto when found")
    void testGetDoctorById() {
        when(doctorRepository.findByIdWithDepartment(1)).thenReturn(Optional.of(sampleDoctor));

        DoctorDto result = doctorService.getDoctorById(1);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).contains("Sarah Connor");
    }

    @Test
    @DisplayName("getDepartments returns active departments")
    void testGetDepartments() {
        sampleDept.setDoctors(List.of(sampleDoctor));
        when(departmentRepository.findAllActiveWithDoctors()).thenReturn(List.of(sampleDept));

        List<DepartmentDto> result = doctorService.getDepartments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Cardiology");
        assertThat(result.get(0).getDoctorCount()).isEqualTo(1);
    }
}
