package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Optional<Patient> findByPatientIdAndIsActiveTrue(Integer patientId);

    Optional<Patient> findByMedicalRecordNumberAndIsActiveTrue(String medicalRecordNumber);

    Optional<Patient> findByEmailIgnoreCaseAndIsActiveTrue(String email);

    @Query("SELECT p FROM Patient p WHERE p.isActive = true AND " +
           "(:search IS NULL OR " +
           "LOWER(p.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.medicalRecordNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Patient> findPatientsPaged(@Param("search") String search, Pageable pageable);

    @Query("SELECT p.medicalRecordNumber FROM Patient p WHERE p.medicalRecordNumber LIKE CONCAT(:prefix, '%')")
    List<String> findMrnsStartingWith(@Param("prefix") String prefix);
}
