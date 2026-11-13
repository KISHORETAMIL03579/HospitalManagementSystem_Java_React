package com.careflow.hospitalmanagement.dto.staff;

import jakarta.validation.constraints.NotBlank;

public class RejectStaffRequest {

    @NotBlank(message = "Reason for rejection is required.")
    private String reason = "";

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
