package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScheduleExceptionType {
    Unavailable(1),
    FullDayLeave(2),
    ExtraWorkingHours(3);

    private final int value;

    ScheduleExceptionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static ScheduleExceptionType fromValue(int value) {
        for (ScheduleExceptionType t : values()) {
            if (t.value == value) {
                return t;
            }
        }
        return Unavailable;
    }
}
