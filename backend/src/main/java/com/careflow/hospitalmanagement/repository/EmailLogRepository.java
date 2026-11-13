package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailLogRepository extends JpaRepository<EmailLog, Integer> {

    Optional<EmailLog> findTopByStaffRegistrationRequestIdOrderBySentAtDesc(Integer staffRegistrationRequestId);
}
