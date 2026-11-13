package com.careflow.hospitalmanagement.dto.appointment;

import com.careflow.hospitalmanagement.domain.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateAppointmentStatusRequest {

    @NotNull(message = "Status is required.")
    private AppointmentStatus status;

    private String notes;

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
