package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Integer> {

    Optional<EmailLog> findTopByStaffRegistrationRequestIdOrderBySentAtDesc(Integer staffRegistrationRequestId);
}
