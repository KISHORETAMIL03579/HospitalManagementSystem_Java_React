package com.careflow.hospitalmanagement.dto.doctor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorAvailabilityDto {

    private int doctorId;
    private String doctorName = "";
    private String specialization = "";
    private String departmentName = "";
    private BigDecimal consultationFee = BigDecimal.ZERO;
    private LocalDate date;
    private String timeZone = "UTC";
    private String availabilityStatus = "Unavailable";
    private String statusMessage = "";
    private LocalTime nextAvailableSlot;
    private int totalSlotsCount;
    private int bookableSlotsCount;
    private int bookedSlotsCount;
    private int blockedSlotsCount;
    private List<TimeSlotDto> slots = new ArrayList<>();

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public LocalTime getNextAvailableSlot() {
        return nextAvailableSlot;
    }

    public void setNextAvailableSlot(LocalTime nextAvailableSlot) {
        this.nextAvailableSlot = nextAvailableSlot;
    }

    public int getTotalSlotsCount() {
        return totalSlotsCount;
    }

    public void setTotalSlotsCount(int totalSlotsCount) {
        this.totalSlotsCount = totalSlotsCount;
    }

    public int getBookableSlotsCount() {
        return bookableSlotsCount;
    }

    public void setBookableSlotsCount(int bookableSlotsCount) {
        this.bookableSlotsCount = bookableSlotsCount;
    }

    public int getBookedSlotsCount() {
        return bookedSlotsCount;
    }

    public void setBookedSlotsCount(int bookedSlotsCount) {
        this.bookedSlotsCount = bookedSlotsCount;
    }

    public int getBlockedSlotsCount() {
        return blockedSlotsCount;
    }

    public void setBlockedSlotsCount(int blockedSlotsCount) {
        this.blockedSlotsCount = blockedSlotsCount;
    }

    public List<TimeSlotDto> getSlots() {
        return slots;
    }

    public void setSlots(List<TimeSlotDto> slots) {
        this.slots = slots;
    }
}
