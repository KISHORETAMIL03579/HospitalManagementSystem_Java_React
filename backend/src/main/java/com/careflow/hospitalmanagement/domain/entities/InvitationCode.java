package com.careflow.hospitalmanagement.domain.entities;

import com.careflow.hospitalmanagement.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitation_codes")
public class InvitationCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_code_id")
    private Integer invitationCodeId;

    @Column(nullable = false, length = 100, unique = true)
    private String code = "";

    @Column(name = "code_hash", nullable = false, length = 500)
    private String codeHash = "";

    @Column(name = "target_role_id", nullable = false)
    private Integer targetRoleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_role_id", insertable = false, updatable = false)
    private Role targetRole;

    @Column(name = "bound_hospital_id", length = 100)
    private String boundHospitalId;

    @Column(name = "bound_email", length = 256)
    private String boundEmail;

    @Column(name = "bound_employee_id", length = 50)
    private String boundEmployeeId;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "used_by_user_id")
    private Integer usedByUserId;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);

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

    public String getCodeHash() {
        return codeHash;
    }

    public void setCodeHash(String codeHash) {
        this.codeHash = codeHash;
    }

    public Integer getTargetRoleId() {
        return targetRoleId;
    }

    public void setTargetRoleId(Integer targetRoleId) {
        this.targetRoleId = targetRoleId;
    }

    public Role getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(Role targetRole) {
        this.targetRole = targetRole;
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
}
