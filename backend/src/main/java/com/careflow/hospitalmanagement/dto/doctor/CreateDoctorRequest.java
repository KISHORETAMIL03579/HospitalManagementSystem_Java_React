package com.careflow.hospitalmanagement.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class CreateDoctorRequest {

    @NotNull(message = "A valid department selection is required.")
    private Integer departmentId;

    @NotBlank(message = "License number is required.")
    @Size(max = 50, message = "License number cannot exceed 50 characters.")
    private String licenseNumber = "";

    @NotBlank(message = "First name is required.")
    @Size(max = 50, message = "First name cannot exceed 50 characters.")
    private String firstName = "";

    @NotBlank(message = "Last name is required.")
    @Size(max = 50, message = "Last name cannot exceed 50 characters.")
    private String lastName = "";

    @NotBlank(message = "Specialization is required.")
    @Size(max = 100, message = "Specialization cannot exceed 100 characters.")
    private String specialization = "";

    @PositiveOrZero(message = "Consultation fee cannot be negative.")
    private BigDecimal consultationFee = BigDecimal.valueOf(100.0);

    @NotBlank(message = "Contact phone number is required.")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters.")
    private String phone = "";

    @Email(message = "Invalid email format.")
    private String email;

    private String availableDays = "Monday,Tuesday,Wednesday,Thursday,Friday";
    private String startTime = "09:00";
    private String endTime = "17:00";

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
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

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
