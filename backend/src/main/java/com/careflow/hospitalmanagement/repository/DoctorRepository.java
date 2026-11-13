package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.department WHERE (:departmentId IS NULL OR d.departmentId = :departmentId) AND d.isActive = true")
    List<Doctor> findAllActiveWithDepartment(@Param("departmentId") Integer departmentId);

    @Query("SELECT d FROM Doctor d LEFT JOIN FETCH d.department WHERE d.doctorId = :doctorId")
    Optional<Doctor> findByIdWithDepartment(@Param("doctorId") Integer doctorId);

    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    Optional<Doctor> findByUserId(Integer userId);
}
