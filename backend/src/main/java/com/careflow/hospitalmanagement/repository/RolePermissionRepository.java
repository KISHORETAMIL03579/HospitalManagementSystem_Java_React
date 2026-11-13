package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermission.RolePermissionId> {
    List<RolePermission> findByRoleId(Integer roleId);
    void deleteByRoleId(Integer roleId);
}
