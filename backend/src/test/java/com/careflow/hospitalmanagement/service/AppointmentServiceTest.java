package com.careflow.hospitalmanagement.service;

import com.careflow.hospitalmanagement.domain.entities.Appointment;
import com.careflow.hospitalmanagement.domain.entities.Doctor;
import com.careflow.hospitalmanagement.domain.entities.Patient;
import com.careflow.hospitalmanagement.domain.entities.User;
import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import com.careflow.hospitalmanagement.dto.appointment.AppointmentDto;
import com.careflow.hospitalmanagement.dto.appointment.CreateAppointmentRequest;
import com.careflow.hospitalmanagement.repository.AppointmentRepository;
import com.careflow.hospitalmanagement.repository.DoctorRepository;
import com.careflow.hospitalmanagement.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setPatientId(1);
        patient.setFirstName("Alice");
        patient.setLastName("Wonder");

        doctor = new Doctor();
        doctor.setDoctorId(2);
        doctor.setFirstName("Bob");
        doctor.setLastName("Physician");
        User docUser = new User();
        docUser.setFullName("Bob Physician");
        doctor.setUser(docUser);

        appointment = new Appointment();
        appointment.setAppointmentId(10);
        appointment.setPatientId(1);
        appointment.setPatient(patient);
        appointment.setDoctorId(2);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(LocalDate.now().plusDays(3));
        appointment.setTimeSlot(LocalTime.of(10, 0));
        appointment.setStatus(AppointmentStatus.Confirmed);
        appointment.setReason("Annual Checkup");
    }

    @Test
    @DisplayName("getAppointments returns list of appointments")
    void testGetAppointments() {
        when(appointmentRepository.findFiltered(1, null, null)).thenReturn(List.of(appointment));

        List<AppointmentDto> dtos = appointmentService.getAppointments(1, null, null);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getPatientName()).isEqualTo("Alice Wonder");
        assertThat(dtos.get(0).getReason()).isEqualTo("Annual Checkup");
    }

    @Test
    @DisplayName("createAppointment throws if slot is already booked")
    void testCreateAppointmentSlotBooked() {
        LocalDate date = LocalDate.now().plusDays(5);
        CreateAppointmentRequest req = new CreateAppointmentRequest();
        req.setPatientId(1);
        req.setDoctorId(2);
        req.setAppointmentDate(date);
        req.setTimeSlot("10:00");
        req.setReason("Checkup");

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.isSlotBooked(2, date, LocalTime.of(10, 0))).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @DisplayName("createAppointment succeeds when slot is available")
    void testCreateAppointmentSuccess() {
        LocalDate date = LocalDate.now().plusDays(5);
        CreateAppointmentRequest req = new CreateAppointmentRequest();
        req.setPatientId(1);
        req.setDoctorId(2);
        req.setAppointmentDate(date);
        req.setTimeSlot("10:00");
        req.setReason("Checkup");

        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(2)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.isSlotBooked(2, date, LocalTime.of(10, 0))).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setAppointmentId(50);
            return a;
        });

        AppointmentDto dto = appointmentService.createAppointment(req);

        assertThat(dto).isNotNull();
        assertThat(dto.getPatientName()).isEqualTo("Alice Wonder");
        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("updateStatus transitions appointment state properly")
    void testUpdateStatus() {
        when(appointmentRepository.findById(10)).thenReturn(Optional.of(appointment));

        appointmentService.updateStatus(10, AppointmentStatus.CheckedIn, "Patient arrived");

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CheckedIn);
        assertThat(appointment.getNotes()).isEqualTo("Patient arrived");
        verify(appointmentRepository).save(appointment);
    }
}
