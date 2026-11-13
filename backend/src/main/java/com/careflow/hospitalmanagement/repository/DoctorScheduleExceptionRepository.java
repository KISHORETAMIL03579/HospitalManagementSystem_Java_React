package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.DoctorScheduleException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleExceptionRepository extends JpaRepository<DoctorScheduleException, Integer> {

    @Query("SELECT e FROM DoctorScheduleException e WHERE e.doctorId = :doctorId AND e.isActive = true AND " +
           "(:fromDate IS NULL OR e.exceptionDate >= :fromDate) AND " +
           "(:toDate IS NULL OR e.exceptionDate <= :toDate) ORDER BY e.exceptionDate ASC, e.startTime ASC")
    List<DoctorScheduleException> findExceptions(
            @Param("doctorId") Integer doctorId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("SELECT e FROM DoctorScheduleException e WHERE e.doctorId = :doctorId AND e.exceptionDate = :date AND e.isActive = true")
    List<DoctorScheduleException> findByDoctorIdAndDate(@Param("doctorId") Integer doctorId, @Param("date") LocalDate date);
}
