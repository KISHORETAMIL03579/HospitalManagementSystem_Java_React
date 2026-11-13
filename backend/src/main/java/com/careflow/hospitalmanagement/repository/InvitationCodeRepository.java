package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.InvitationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InvitationCodeRepository extends JpaRepository<InvitationCode, Integer> {

    @Query("SELECT i FROM InvitationCode i LEFT JOIN FETCH i.targetRole WHERE i.code = :code")
    Optional<InvitationCode> findByCode(@Param("code") String code);
}
