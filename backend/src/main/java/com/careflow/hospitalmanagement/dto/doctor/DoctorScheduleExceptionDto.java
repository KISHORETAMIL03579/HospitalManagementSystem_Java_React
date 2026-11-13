package com.careflow.hospitalmanagement.dto.doctor;

import com.careflow.hospitalmanagement.domain.enums.ScheduleExceptionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DoctorScheduleExceptionDto {

    private int exceptionId;
    private int doctorId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private ScheduleExceptionType exceptionType;
    private String reason;
    private LocalDateTime createdAt;

    public int getExceptionId() {
        return exceptionId;
    }

    public void setExceptionId(int exceptionId) {
        this.exceptionId = exceptionId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
