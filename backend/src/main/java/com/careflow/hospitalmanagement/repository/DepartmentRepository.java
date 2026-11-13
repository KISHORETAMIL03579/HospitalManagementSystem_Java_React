package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    Optional<Department> findByNameIgnoreCase(String name);

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.doctors doc WHERE d.isActive = true")
    List<Department> findAllActiveWithDoctors();
}
