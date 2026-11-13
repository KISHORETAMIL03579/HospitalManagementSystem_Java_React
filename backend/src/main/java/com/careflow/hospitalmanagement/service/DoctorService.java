package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Department;
import com.careflow.hospitalmanagement.domain.entities.Doctor;
import com.careflow.hospitalmanagement.dto.doctor.CreateDoctorRequest;
import com.careflow.hospitalmanagement.dto.doctor.DepartmentDto;
import com.careflow.hospitalmanagement.dto.doctor.DoctorDto;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.DepartmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;

    public DoctorService(DoctorRepository doctorRepository, DepartmentRepository departmentRepository) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DoctorDto> getDoctors(Integer departmentId) {
        List<Doctor> doctors = doctorRepository.findAllActiveWithDepartment(departmentId);
        List<DoctorDto> dtos = new ArrayList<>();
        for (Doctor d : doctors) {
            dtos.add(mapToDto(d));
        }
        return dtos;
    }

    @Transactional(readOnly = true)
    public DoctorDto getDoctorById(int doctorId) {
        return doctorRepository.findByIdWithDepartment(doctorId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getDepartments() {
        List<Department> departments = departmentRepository.findAllActiveWithDoctors();
        List<DepartmentDto> dtos = new ArrayList<>();
        for (Department d : departments) {
            DepartmentDto dto = new DepartmentDto();
            dto.setDepartmentId(d.getDepartmentId());
            dto.setName(d.getName());
            dto.setCode(d.getCode());
            dto.setDescription(d.getDescription());
            dto.setDoctorCount(d.getDoctors() != null ? d.getDoctors().size() : 0);
            dtos.add(dto);
        }
        return dtos;
    }

    public DoctorDto createDoctor(CreateDoctorRequest request) {
        Department dept = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + request.getDepartmentId()));

        Doctor d = new Doctor();
        d.setDepartmentId(dept.getDepartmentId());
        d.setDepartment(dept);
        d.setLicenseNumber(request.getLicenseNumber().trim());
        d.setFirstName(request.getFirstName().trim());
        d.setLastName(request.getLastName().trim());
        d.setSpecialization(request.getSpecialization().trim());
        d.setConsultationFee(request.getConsultationFee());
        d.setPhone(request.getPhone().trim());
        d.setEmail(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail().trim() : null);
        d.setAvailableDays(request.getAvailableDays());
        d.setStartTime(LocalTime.parse(request.getStartTime()));
        d.setEndTime(LocalTime.parse(request.getEndTime()));
        d.setActive(true);

        Doctor saved = doctorRepository.save(d);
        return mapToDto(saved);
    }

    public DoctorDto mapToDto(Doctor d) {
        DoctorDto dto = new DoctorDto();
        dto.setDoctorId(d.getDoctorId());
        dto.setDepartmentId(d.getDepartmentId());
        dto.setDepartmentName(d.getDepartment() != null ? d.getDepartment().getName() : "General");
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setFirstName(d.getFirstName());
        dto.setLastName(d.getLastName());
        dto.setFullName(d.getFullName());
        dto.setSpecialization(d.getSpecialization());
        dto.setConsultationFee(d.getConsultationFee());
        dto.setPhone(d.getPhone());
        dto.setEmail(d.getEmail());
        dto.setAvailableDays(d.getAvailableDays() != null ? d.getAvailableDays() : "Monday-Friday");
        dto.setStartTime(d.getStartTime() != null ? d.getStartTime().toString() : "09:00");
        dto.setEndTime(d.getEndTime() != null ? d.getEndTime().toString() : "17:00");
        dto.setActive(d.isActive());
        return dto;
    }
}
