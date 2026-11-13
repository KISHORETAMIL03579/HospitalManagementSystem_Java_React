package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BloodGroup {
    Unknown(0),
    APositive(1),
    ANegative(2),
    BPositive(3),
    BNegative(4),
    ABPositive(5),
    ABNegative(6),
    OPositive(7),
    ONegative(8);

    private final int value;

    BloodGroup(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static BloodGroup fromValue(int value) {
        for (BloodGroup bg : values()) {
            if (bg.value == value) {
                return bg;
            }
        }
        return Unknown;
    }
}
