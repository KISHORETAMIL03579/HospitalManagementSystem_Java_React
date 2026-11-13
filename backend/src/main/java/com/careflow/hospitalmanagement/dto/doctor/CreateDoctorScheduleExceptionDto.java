package com.careflow.hospitalmanagement.dto.doctor;

import com.careflow.hospitalmanagement.domain.enums.ScheduleExceptionType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateDoctorScheduleExceptionDto {

    @NotNull(message = "Date is required.")
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleExceptionType exceptionType = ScheduleExceptionType.Unavailable;
    private String reason;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public ScheduleExceptionType getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(ScheduleExceptionType exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
