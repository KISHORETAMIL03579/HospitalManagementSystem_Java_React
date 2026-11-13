package com.careflow.hospitalmanagement.dto.admin;

public class UpdateUserStatusRequest {

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
