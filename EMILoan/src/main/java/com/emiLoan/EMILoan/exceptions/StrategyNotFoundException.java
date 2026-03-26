package com.emiLoan.EMILoan.exceptions;


import org.springframework.http.HttpStatus;

public class StrategyNotFoundException extends ApplicationException {

    public StrategyNotFoundException(String strategyName) {
        super(String.format("EMI calculation strategy '%s' is not supported or implemented.", strategyName),
                "STRATEGY_NOT_IMPLEMENTED",
                HttpStatus.BAD_REQUEST);
    }
}