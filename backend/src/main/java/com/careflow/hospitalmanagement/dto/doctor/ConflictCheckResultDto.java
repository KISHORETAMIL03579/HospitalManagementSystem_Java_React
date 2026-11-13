package com.careflow.hospitalmanagement.dto.doctor;

import java.util.ArrayList;
import java.util.List;

public class ConflictCheckResultDto {

    private boolean hasConflicts;
    private int conflictCount;
    private List<ConflictingAppointmentDto> conflictingAppointments = new ArrayList<>();
    private String message = "";

    public boolean isHasConflicts() {
        return hasConflicts;
    }

    public void setHasConflicts(boolean hasConflicts) {
        this.hasConflicts = hasConflicts;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(int conflictCount) {
        this.conflictCount = conflictCount;
    }

    public List<ConflictingAppointmentDto> getConflictingAppointments() {
        return conflictingAppointments;
    }

    public void setConflictingAppointments(List<ConflictingAppointmentDto> conflictingAppointments) {
        this.conflictingAppointments = conflictingAppointments;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
