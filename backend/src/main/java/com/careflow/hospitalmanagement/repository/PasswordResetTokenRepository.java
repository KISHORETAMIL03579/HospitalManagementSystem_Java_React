package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findTopByEmailIgnoreCaseAndIsUsedFalseOrderByCreatedAtDesc(String email);
}
