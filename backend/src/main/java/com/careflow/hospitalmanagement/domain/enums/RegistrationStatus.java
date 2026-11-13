package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RegistrationStatus {
    Pending(0),
    Approved(1),
    Rejected(2);

    private final int value;

    RegistrationStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static RegistrationStatus fromValue(int value) {
        for (RegistrationStatus s : values()) {
            if (s.value == value) {
                return s;
            }
        }
        return Pending;
    }
}
