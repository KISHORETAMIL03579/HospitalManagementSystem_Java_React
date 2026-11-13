package com.careflow.hospitalmanagement.dto.appointment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateAppointmentRequest {

    private int patientId;

    @NotNull(message = "Valid doctor selection is required.")
    @Positive(message = "Valid doctor selection is required.")
    private Integer doctorId;

    @NotNull(message = "Appointment date is required.")
    private LocalDate appointmentDate;

    @NotBlank(message = "Time slot is required.")
    private String timeSlot = "09:00";

    @NotBlank(message = "Reason for visit is required.")
    @Size(max = 200, message = "Reason for visit cannot exceed 200 characters.")
    private String reason = "";

    private String notes;

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public Integer getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Integer doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
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
