package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailDeliveryStatus {
    Queued(0),
    Sent(1),
    Delivered(2),
    Failed(3),
    Bounced(4);

    private final int value;

    EmailDeliveryStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @JsonValue
    public int toValue() {
        return this.value;
    }

    public static EmailDeliveryStatus fromValue(int value) {
        for (EmailDeliveryStatus s : values()) {
            if (s.value == value) {
                return s;
            }
        }
        return Queued;
    }

    @JsonCreator
    public static EmailDeliveryStatus from(Object value) {
        if (value == null) {
            return Queued;
        }
        if (value instanceof Number n) {
            return fromValue(n.intValue());
        }
        String str = value.toString().trim();
        for (EmailDeliveryStatus s : values()) {
            if (s.name().equalsIgnoreCase(str) || String.valueOf(s.value).equals(str)) {
                return s;
            }
        }
        return Queued;
    }
}
