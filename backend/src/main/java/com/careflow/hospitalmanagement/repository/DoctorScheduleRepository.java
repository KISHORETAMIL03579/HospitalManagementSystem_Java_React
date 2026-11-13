package com.careflow.hospitalmanagement.repository;

import com.careflow.hospitalmanagement.domain.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Integer> {

    List<DoctorSchedule> findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(Integer doctorId);

    void deleteByDoctorId(Integer doctorId);
}
