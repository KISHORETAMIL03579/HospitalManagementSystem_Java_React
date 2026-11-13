package com.careflow.hospitalmanagement.dto.staff;

public class UpdateStaffRegistrationRequest {

    private String fullName;
    private String email;
    private String employeeId;
    private Integer requestedRoleId;
    private Integer departmentId;
    private Integer status;
    private String rejectionReason;

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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getRequestedRoleId() {
        return requestedRoleId;
    }

    public void setRequestedRoleId(Integer requestedRoleId) {
        this.requestedRoleId = requestedRoleId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}
