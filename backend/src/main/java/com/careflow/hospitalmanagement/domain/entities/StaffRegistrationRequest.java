package com.careflow.hospitalmanagement.domain.entities;

import com.careflow.hospitalmanagement.common.BaseEntity;
import com.careflow.hospitalmanagement.domain.enums.RegistrationStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_registration_requests")
public class StaffRegistrationRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_registration_request_id")
    private Integer staffRegistrationRequestId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName = "";

    @Column(nullable = false, length = 256)
    private String email = "";

    @Column(nullable = false, length = 100)
    private String username = "";

    @Column(name = "password_hash", nullable = false, length = 500)
    private String passwordHash = "";

    @Column(name = "employee_id", length = 50)
    private String employeeId;

    @Column(name = "department_id")
    private Integer departmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;

    @Column(name = "requested_role_id", nullable = false)
    private Integer requestedRoleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requested_role_id", insertable = false, updatable = false)
    private Role requestedRole;

    @Column(name = "invitation_code", length = 100)
    private String invitationCode = "";

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.Pending;

    @Column(name = "reviewed_by_user_id")
    private Integer reviewedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id", insertable = false, updatable = false)
    private User reviewedByUser;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "rejection_reason", length = 1000)
    private String rejectionReason;

    public Integer getStaffRegistrationRequestId() {
        return staffRegistrationRequestId;
    }

    public void setStaffRegistrationRequestId(Integer staffRegistrationRequestId) {
        this.staffRegistrationRequestId = staffRegistrationRequestId;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Integer getRequestedRoleId() {
        return requestedRoleId;
    }

    public void setRequestedRoleId(Integer requestedRoleId) {
        this.requestedRoleId = requestedRoleId;
    }

    public Role getRequestedRole() {
        return requestedRole;
    }

    public void setRequestedRole(Role requestedRole) {
        this.requestedRole = requestedRole;
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

    public Integer getReviewedByUserId() {
        return reviewedByUserId;
    }

    public void setReviewedByUserId(Integer reviewedByUserId) {
        this.reviewedByUserId = reviewedByUserId;
    }

    public User getReviewedByUser() {
        return reviewedByUser;
    }

    public void setReviewedByUser(User reviewedByUser) {
        this.reviewedByUser = reviewedByUser;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
