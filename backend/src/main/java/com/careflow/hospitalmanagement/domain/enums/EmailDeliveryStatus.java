package com.careflow.hospitalmanagement.domain.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EmailDeliveryStatus {
    Queued(0),
    Sent(1),
    Delivered(2),
    Failed(3),
    Bounced(4),
    Retrying(5);

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
        return Sent;
    }
}
