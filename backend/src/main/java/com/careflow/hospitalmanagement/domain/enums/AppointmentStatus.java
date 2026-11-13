package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AppointmentStatus {
    Pending(0),
    Confirmed(10),
    CheckedIn(20),
    InConsultation(30),
    Completed(40),
    Cancelled(50),
    NoShow(60);

    private final int value;

    AppointmentStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static AppointmentStatus fromValue(int value) {
        for (AppointmentStatus s : values()) {
            if (s.value == value) {
                return s;
            }
        }
        return Confirmed;
    }
}
