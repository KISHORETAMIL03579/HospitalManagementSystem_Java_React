package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    Admin(100),
    HospitalManager(90),
    Doctor(80),
    Pharmacist(75),
    LabTechnician(70),
    Nurse(60),
    Receptionist(40),
    Patient(20);

    private final int level;

    UserRole(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    @JsonValue
    public int toValue() {
        return this.level;
    }

    public static UserRole fromLevel(int level) {
        for (UserRole role : values()) {
            if (role.level == level) {
                return role;
            }
        }
        return Receptionist;
    }

    public static UserRole fromString(String name) {
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        return Receptionist;
    }
}
