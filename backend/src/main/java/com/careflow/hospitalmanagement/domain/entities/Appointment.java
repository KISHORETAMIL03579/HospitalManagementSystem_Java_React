package com.careflow.hospitalmanagement.domain.entities;

import com.careflow.hospitalmanagement.common.BaseEntity;
import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", insertable = false, updatable = false)
    private Patient patient;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "time_slot", nullable = false)
    private LocalTime timeSlot;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.Confirmed;

    @Column(nullable = false, length = 500)
    private String reason = "";

    @Column(length = 1000)
    private String notes;

    public void confirm() {
        if (status != AppointmentStatus.Pending) {
            throw new IllegalStateException("Only pending appointments can be confirmed.");
        }
        status = AppointmentStatus.Confirmed;
    }

    public void checkIn() {
        if (status != AppointmentStatus.Confirmed && status != AppointmentStatus.Pending) {
            throw new IllegalStateException("Only confirmed or pending appointments can be checked in.");
        }
        status = AppointmentStatus.CheckedIn;
    }

    public void startConsultation() {
        if (status != AppointmentStatus.CheckedIn) {
            throw new IllegalStateException("Patient must be checked in before starting consultation.");
        }
        status = AppointmentStatus.InConsultation;
    }

    public void complete() {
        if (status != AppointmentStatus.InConsultation) {
            throw new IllegalStateException("Only an active consultation can be completed.");
        }
        status = AppointmentStatus.Completed;
    }

    public void cancel() {
        if (status == AppointmentStatus.Completed || status == AppointmentStatus.Cancelled) {
            throw new IllegalStateException("Completed or cancelled appointments cannot be cancelled.");
        }
        status = AppointmentStatus.Cancelled;
    }

    public void markNoShow() {
        if (status == AppointmentStatus.Completed || status == AppointmentStatus.Cancelled) {
            throw new IllegalStateException("Cannot mark completed or cancelled appointment as no-show.");
        }
        status = AppointmentStatus.NoShow;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocalTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
