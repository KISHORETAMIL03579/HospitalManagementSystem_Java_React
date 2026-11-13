package com.careflow.hospitalmanagement.dto.doctor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorDirectoryItemDto {

    private int doctorId;
    private String fullName = "";
    private String specialization = "";
    private int departmentId;
    private String departmentName = "";
    private String licenseNumber = "";
    private BigDecimal consultationFee = BigDecimal.ZERO;
    private String phone = "";
    private String email;
    private int yearsOfExperience = 8;
    private double rating = 4.9;
    private int reviewCount = 34;
    private String languages = "English";
    private String biography = "";
    private String availabilityStatus = "Unavailable";
    private String statusBadge = "Unavailable";
    private String nextAvailableSlotText;
    private LocalDate nextAvailableDate;
    private LocalTime nextAvailableTime;
    private boolean isActive = true;

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public String getStatusBadge() {
        return statusBadge;
    }

    public void setStatusBadge(String statusBadge) {
        this.statusBadge = statusBadge;
    }

    public String getNextAvailableSlotText() {
        return nextAvailableSlotText;
    }

    public void setNextAvailableSlotText(String nextAvailableSlotText) {
        this.nextAvailableSlotText = nextAvailableSlotText;
    }

    public LocalDate getNextAvailableDate() {
        return nextAvailableDate;
    }

    public void setNextAvailableDate(LocalDate nextAvailableDate) {
        this.nextAvailableDate = nextAvailableDate;
    }

    public LocalTime getNextAvailableTime() {
        return nextAvailableTime;
    }

    public void setNextAvailableTime(LocalTime nextAvailableTime) {
        this.nextAvailableTime = nextAvailableTime;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
