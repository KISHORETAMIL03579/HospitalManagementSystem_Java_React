package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Optional<Permission> findByName(String name);
}
