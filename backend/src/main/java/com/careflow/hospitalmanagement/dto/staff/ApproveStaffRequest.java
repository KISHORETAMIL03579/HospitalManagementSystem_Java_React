package com.careflow.hospitalmanagement.dto.staff;

public class ApproveStaffRequest {

    private Integer authorizedRoleId;
    private String notes;

    public Integer getAuthorizedRoleId() {
        return authorizedRoleId;
    }

    public void setAuthorizedRoleId(Integer authorizedRoleId) {
        this.authorizedRoleId = authorizedRoleId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
