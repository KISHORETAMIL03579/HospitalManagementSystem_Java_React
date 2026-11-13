package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.StaffRegistrationRequest;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRegistrationRequestRepository extends JpaRepository<StaffRegistrationRequest, Integer> {

    Optional<StaffRegistrationRequest> findByEmailIgnoreCase(String email);

    Optional<StaffRegistrationRequest> findTopByEmailIgnoreCaseOrderByCreatedAtDesc(String email);

    @Query("SELECT COUNT(s) > 0 FROM StaffRegistrationRequest s WHERE LOWER(s.email) = LOWER(:email) AND s.status = com.careflow.hospitalmanagement.domain.enums.RegistrationStatus.Pending")
    boolean existsPendingByEmail(@Param("email") String email);

    @Query("SELECT s FROM StaffRegistrationRequest s LEFT JOIN FETCH s.department LEFT JOIN FETCH s.requestedRole LEFT JOIN FETCH s.reviewedByUser WHERE (:status IS NULL OR s.status = :status) ORDER BY s.createdAt DESC")
    List<StaffRegistrationRequest> findAllFiltered(@Param("status") RegistrationStatus status);

    @Query("SELECT s FROM StaffRegistrationRequest s LEFT JOIN FETCH s.department LEFT JOIN FETCH s.requestedRole LEFT JOIN FETCH s.reviewedByUser WHERE s.staffRegistrationRequestId = :id")
    Optional<StaffRegistrationRequest> findByIdWithDetails(@Param("id") Integer id);
}
