package com.careflow.hospitalmanagement.exception;

public class AccountPendingApprovalException extends RuntimeException {
    public AccountPendingApprovalException(String message) {
        super(message);
    }
}
