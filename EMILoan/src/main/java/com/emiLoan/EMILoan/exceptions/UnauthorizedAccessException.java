package com.emiLoan.EMILoan.exceptions;


import org.springframework.http.HttpStatus;

public class UnauthorizedAccessException extends ApplicationException {

    public UnauthorizedAccessException(String message) {
        super(message, "CROSS_USER_ACCESS_DENIED", HttpStatus.FORBIDDEN);
    }

    public UnauthorizedAccessException(Object userId, Object resourceId) {
        super(String.format("User %s is not authorized to access resource %s", userId, resourceId),
                "UNAUTHORIZED_RESOURCE_ACCESS",
                HttpStatus.FORBIDDEN);
    }
}