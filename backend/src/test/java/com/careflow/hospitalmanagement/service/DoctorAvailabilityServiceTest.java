package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.*;
import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import com.careflow.hospitalmanagement.domain.enums.ScheduleExceptionType;
import com.careflow.hospitalmanagement.dto.doctor.*;
import com.careflow.hospitalmanagement.exception.ResourceNotFoundException;
import com.careflow.hospitalmanagement.repository.AppointmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import com.careflow.hospitalmanagement.repository.DoctorScheduleExceptionRepository;
import com.careflow.hospitalmanagement.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository scheduleRepository;

    @Mock
    private DoctorScheduleExceptionRepository exceptionRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private DoctorAvailabilityService availabilityService;

    private int doctorId = 5;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setDoctorId(doctorId);
        doctor.setFirstName("Gregory");
        doctor.setLastName("House");
        doctor.setSpecialization("Diagnostics");
        doctor.setConsultationFee(new BigDecimal("200.00"));
        doctor.setActive(true);

        Department dept = new Department();
        dept.setName("Diagnostics");
        doctor.setDepartment(dept);

        Instant fixedInstant = Instant.parse("2026-10-01T08:00:00Z");
        availabilityService.setClock(Clock.fixed(fixedInstant, ZoneId.of("UTC")));
    }

    @Test
    @DisplayName("getDoctorAvailability should throw ResourceNotFoundException for unknown doctor")
    void testUnknownDoctor() {
        when(doctorRepository.findByIdWithDepartment(doctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> availabilityService.getDoctorAvailability(doctorId, LocalDate.of(2026, 10, 19)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getDoctorAvailability should return available slots based on schedule")
    void testAvailableSlots() {
        LocalDate date = LocalDate.of(2026, 10, 19); // Monday

        when(doctorRepository.findByIdWithDepartment(doctorId)).thenReturn(Optional.of(doctor));

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(doctorId);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);

        when(scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(doctorId))
                .thenReturn(List.of(schedule));
        when(exceptionRepository.findByDoctorIdAndDate(doctorId, date))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.findFiltered(null, doctorId, date))
                .thenReturn(Collections.emptyList());

        DoctorAvailabilityDto result = availabilityService.getDoctorAvailability(doctorId, date);

        assertThat(result).isNotNull();
        assertThat(result.getSlots()).hasSize(4);
        assertThat(result.getBookableSlotsCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("getDoctorAvailability should reflect booked slots")
    void testBookedSlots() {
        LocalDate date = LocalDate.of(2026, 10, 19);

        when(doctorRepository.findByIdWithDepartment(doctorId)).thenReturn(Optional.of(doctor));

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(doctorId);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setSlotDurationMinutes(30);
        schedule.setActive(true);

        when(scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(doctorId))
                .thenReturn(List.of(schedule));
        when(exceptionRepository.findByDoctorIdAndDate(doctorId, date))
                .thenReturn(Collections.emptyList());

        Appointment appt = new Appointment();
        appt.setTimeSlot(LocalTime.of(9, 0));
        appt.setStatus(AppointmentStatus.Confirmed);
        appt.setActive(true);

        when(appointmentRepository.findFiltered(null, doctorId, date))
                .thenReturn(List.of(appt));

        DoctorAvailabilityDto result = availabilityService.getDoctorAvailability(doctorId, date);

        assertThat(result.getSlots()).hasSize(2);
        assertThat(result.getSlots().get(0).isBookable()).isFalse();
        assertThat(result.getSlots().get(0).getStatus()).isEqualTo("Booked");
        assertThat(result.getSlots().get(1).isBookable()).isTrue();
        assertThat(result.getBookableSlotsCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getDoctorAvailability should handle leave exception")
    void testLeaveException() {
        LocalDate date = LocalDate.of(2026, 10, 19);

        when(doctorRepository.findByIdWithDepartment(doctorId)).thenReturn(Optional.of(doctor));

        DoctorScheduleException leaveEx = new DoctorScheduleException();
        leaveEx.setDoctorId(doctorId);
        leaveEx.setExceptionDate(date);
        leaveEx.setExceptionType(ScheduleExceptionType.FullDayLeave);
        leaveEx.setReason("Annual conference");
        leaveEx.setActive(true);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDoctorId(doctorId);
        schedule.setDayOfWeek(DayOfWeek.MONDAY);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setActive(true);

        when(scheduleRepository.findByDoctorIdAndIsActiveTrueOrderByStartTimeAsc(doctorId))
                .thenReturn(List.of(schedule));
        when(exceptionRepository.findByDoctorIdAndDate(doctorId, date))
                .thenReturn(List.of(leaveEx));
        when(appointmentRepository.findFiltered(null, doctorId, date))
                .thenReturn(Collections.emptyList());

        DoctorAvailabilityDto result = availabilityService.getDoctorAvailability(doctorId, date);

        assertThat(result.getBookableSlotsCount()).isEqualTo(0);
        assertThat(result.getSlots().stream().allMatch(s -> !s.isBookable())).isTrue();
    }
}
