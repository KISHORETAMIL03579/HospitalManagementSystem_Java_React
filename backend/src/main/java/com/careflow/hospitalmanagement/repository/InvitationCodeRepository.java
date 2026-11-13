package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Integer> {

    @Query("SELECT i FROM InvitationCode i LEFT JOIN FETCH i.targetRole WHERE i.code = :code")
    Optional<InvitationCode> findByCode(@Param("code") String code);

    boolean existsByCode(String code);

    @Query("SELECT i FROM InvitationCode i LEFT JOIN FETCH i.targetRole ORDER BY i.createdAt DESC")
    List<InvitationCode> findAllWithRole();
}
