package com.careflow.hospitalmanagement.common;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    @JsonCreator
    public static TimeFormat from(Object value) {
        if (value == null) {
            return Hour12;
        }
        if (value instanceof Number n) {
            return fromValue(n.intValue());
        }
        String str = value.toString().trim();
        for (TimeFormat tf : values()) {
            if (tf.name().equalsIgnoreCase(str) || String.valueOf(tf.value).equals(str)) {
                return tf;
            }
        }
        return Hour12;
    }
}
