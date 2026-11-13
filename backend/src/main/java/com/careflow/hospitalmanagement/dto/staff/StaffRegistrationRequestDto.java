package com.careflow.hospitalmanagement.dto.staff;

import com.careflow.hospitalmanagement.domain.enums.EmailDeliveryStatus;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import java.time.LocalDateTime;

public class StaffRegistrationRequestDto {

    private int id;
    private String fullName = "";
    private String email = "";
    private String username = "";
    private String employeeId;
    private Integer departmentId;
    private String departmentName;
    private int requestedRoleId;
    private String requestedRoleName = "";
    private String invitationCode = "";
    private RegistrationStatus status = RegistrationStatus.Pending;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByName;
    private String rejectionReason;

    // Email Delivery Tracking Fields
    private EmailDeliveryStatus emailStatus = EmailDeliveryStatus.Sent;
    private LocalDateTime lastEmailAttempt;
    private String emailErrorMessage;
    private Integer emailLogId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public int getRequestedRoleId() {
        return requestedRoleId;
    }

    public void setRequestedRoleId(int requestedRoleId) {
        this.requestedRoleId = requestedRoleId;
    }

    public String getRequestedRoleName() {
        return requestedRoleName;
    }

    public void setRequestedRoleName(String requestedRoleName) {
        this.requestedRoleName = requestedRoleName;
    }

    public String getInvitationCode() {
        return invitationCode;
    }

    public void setInvitationCode(String invitationCode) {
        this.invitationCode = invitationCode;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public EmailDeliveryStatus getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(EmailDeliveryStatus emailStatus) {
        this.emailStatus = emailStatus;
    }

    public LocalDateTime getLastEmailAttempt() {
        return lastEmailAttempt;
    }

    public void setLastEmailAttempt(LocalDateTime lastEmailAttempt) {
        this.lastEmailAttempt = lastEmailAttempt;
    }

    public String getEmailErrorMessage() {
        return emailErrorMessage;
    }

    public void setEmailErrorMessage(String emailErrorMessage) {
        this.emailErrorMessage = emailErrorMessage;
    }

    public Integer getEmailLogId() {
        return emailLogId;
    }

    public void setEmailLogId(Integer emailLogId) {
        this.emailLogId = emailLogId;
    }
}
