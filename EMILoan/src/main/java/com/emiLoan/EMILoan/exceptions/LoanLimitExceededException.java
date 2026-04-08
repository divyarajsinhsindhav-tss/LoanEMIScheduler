package com.emiLoan.EMILoan.exceptions;


import org.springframework.http.HttpStatus;

public class LoanLimitExceededException extends ApplicationException {

    private static final String DEFAULT_MESSAGE = "Maximum active loan limit (3) reached";

    public LoanLimitExceededException() {
        super(DEFAULT_MESSAGE, "LOAN_LIMIT_EXCEEDED", HttpStatus.BAD_REQUEST);
    }

    public LoanLimitExceededException(Long currentActiveLoans) {
        super(String.format("%s. Current active loans: %d", DEFAULT_MESSAGE, currentActiveLoans),
                "LOAN_LIMIT_EXCEEDED",
                HttpStatus.BAD_REQUEST);
    }
}