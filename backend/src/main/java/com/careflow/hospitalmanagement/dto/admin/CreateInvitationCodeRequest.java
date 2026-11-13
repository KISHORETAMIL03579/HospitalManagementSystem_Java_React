package com.careflow.hospitalmanagement.dto.admin;

import jakarta.validation.constraints.NotNull;

public class CreateInvitationCodeRequest {

    private String code;

    @NotNull(message = "Target role is required.")
    private Integer targetRoleId;

    private Integer expiresInDays = 30;
    private String boundEmail;
    private String boundEmployeeId;
    private String boundHospitalId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getTargetRoleId() {
        return targetRoleId;
    }

    public void setTargetRoleId(Integer targetRoleId) {
        this.targetRoleId = targetRoleId;
    }

    public Integer getExpiresInDays() {
        return expiresInDays != null ? expiresInDays : 30;
    }

    public void setExpiresInDays(Integer expiresInDays) {
        this.expiresInDays = expiresInDays;
    }

    public String getBoundEmail() {
        return boundEmail;
    }

    public void setBoundEmail(String boundEmail) {
        this.boundEmail = boundEmail;
    }

    public String getBoundEmployeeId() {
        return boundEmployeeId;
    }

    public void setBoundEmployeeId(String boundEmployeeId) {
        this.boundEmployeeId = boundEmployeeId;
    }

    public String getBoundHospitalId() {
        return boundHospitalId;
    }

    public void setBoundHospitalId(String boundHospitalId) {
        this.boundHospitalId = boundHospitalId;
    }
}
