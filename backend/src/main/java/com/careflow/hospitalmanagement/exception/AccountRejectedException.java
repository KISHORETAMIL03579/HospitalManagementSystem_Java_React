package com.careflow.hospitalmanagement.exception;

public class AccountRejectedException extends RuntimeException {
    public AccountRejectedException(String message) {
        super(message);
    }
}
