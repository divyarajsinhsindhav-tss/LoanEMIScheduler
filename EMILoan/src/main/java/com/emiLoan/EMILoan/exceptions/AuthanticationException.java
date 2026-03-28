package com.emiLoan.EMILoan.exceptions;

import org.springframework.http.HttpStatus;

public class AuthanticationException extends ApplicationException {

    public AuthanticationException(String message) {
        super(message, "AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED);
    }
}
