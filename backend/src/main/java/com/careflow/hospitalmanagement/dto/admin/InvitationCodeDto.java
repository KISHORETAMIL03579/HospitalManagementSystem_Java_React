package com.careflow.hospitalmanagement.dto.admin;

import java.time.LocalDateTime;

public class InvitationCodeDto {

    private Integer invitationCodeId;
    private String code;
    private Integer targetRoleId;
    private String targetRoleName;
    private String boundHospitalId;
    private String boundEmail;
    private String boundEmployeeId;
    private boolean isUsed;
    private LocalDateTime usedAt;
    private Integer usedByUserId;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    public Integer getInvitationCodeId() {
        return invitationCodeId;
    }

    public void setInvitationCodeId(Integer invitationCodeId) {
        this.invitationCodeId = invitationCodeId;
    }

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

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public void setTargetRoleName(String targetRoleName) {
        this.targetRoleName = targetRoleName;
    }

    public String getBoundHospitalId() {
        return boundHospitalId;
    }

    public void setBoundHospitalId(String boundHospitalId) {
        this.boundHospitalId = boundHospitalId;
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

    public boolean isUsed() {
        return isUsed;
    }

    public void setUsed(boolean used) {
        isUsed = used;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public Integer getUsedByUserId() {
        return usedByUserId;
    }

    public void setUsedByUserId(Integer usedByUserId) {
        this.usedByUserId = usedByUserId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
