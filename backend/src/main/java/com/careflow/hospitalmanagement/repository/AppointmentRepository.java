package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.department WHERE " +
           "(:patientId IS NULL OR a.patientId = :patientId) AND " +
           "(:doctorId IS NULL OR a.doctorId = :doctorId) AND " +
           "(:date IS NULL OR a.appointmentDate = :date) AND " +
           "a.isActive = true ORDER BY a.appointmentDate DESC, a.timeSlot ASC")
    List<Appointment> findFiltered(
            @Param("patientId") Integer patientId,
            @Param("doctorId") Integer doctorId,
            @Param("date") LocalDate date);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.department WHERE a.appointmentId = :id")
    Optional<Appointment> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctorId = :doctorId AND a.appointmentDate = :date AND a.timeSlot = :timeSlot AND a.isActive = true AND a.status NOT IN (com.careflow.hospitalmanagement.domain.enums.AppointmentStatus.Cancelled, com.careflow.hospitalmanagement.domain.enums.AppointmentStatus.NoShow)")
    boolean isSlotBooked(
            @Param("doctorId") Integer doctorId,
            @Param("date") LocalDate date,
            @Param("timeSlot") LocalTime timeSlot);

    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.patient LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH d.department WHERE a.patientId = :patientId AND a.isActive = true ORDER BY a.appointmentDate DESC, a.timeSlot ASC")
    List<Appointment> findByPatientId(@Param("patientId") Integer patientId);
}
