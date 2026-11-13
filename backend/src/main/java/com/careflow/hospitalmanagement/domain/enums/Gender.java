package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    Male(1),
    Female(2),
    Other(3),
    Unspecified(4);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static Gender fromValue(int value) {
        for (Gender g : values()) {
            if (g.value == value) {
                return g;
            }
        }
        return Other;
    }

    @JsonCreator
    public static Gender from(Object value) {
        if (value == null) {
            return Other;
        }
        if (value instanceof Number n) {
            return fromValue(n.intValue());
        }
        String str = value.toString().trim();
        for (Gender g : values()) {
            if (g.name().equalsIgnoreCase(str) || String.valueOf(g.value).equals(str)) {
                return g;
            }
        }
        return Other;
    }
}
