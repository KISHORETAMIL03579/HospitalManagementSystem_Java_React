package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission WHERE LOWER(u.username) = LOWER(:identifier) OR LOWER(u.email) = LOWER(:identifier)")
    Optional<User> findByEmailOrUsernameWithRole(@Param("identifier") String identifier);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.rolePermissions rp LEFT JOIN FETCH rp.permission WHERE u.userId = :userId")
    Optional<User> findByIdWithRole(@Param("userId") Integer userId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    @Query("SELECT COUNT(u) FROM User u JOIN u.role r WHERE LOWER(r.name) = 'admin' AND u.isActive = true")
    long countActiveAdmins();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE " +
           "(:search IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:roleId IS NULL OR u.roleId = :roleId) AND " +
           "(:isActive IS NULL OR u.isActive = :isActive) ORDER BY u.userId ASC")
    List<User> searchUsers(@Param("search") String search, @Param("roleId") Integer roleId, @Param("isActive") Boolean isActive);
}
