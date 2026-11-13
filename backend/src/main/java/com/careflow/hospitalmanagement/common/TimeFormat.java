package com.careflow.hospitalmanagement.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeFormat {
    Hour12(12),
    Hour24(24);

    private final int value;

    TimeFormat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static TimeFormat fromValue(int value) {
        for (TimeFormat tf : values()) {
            if (tf.value == value) {
                return tf;
            }
        }
        return Hour12;
    }
}
