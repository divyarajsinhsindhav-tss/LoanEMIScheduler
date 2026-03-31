package com.emiLoan.EMILoan.common.constants;

import com.emiLoan.EMILoan.common.enums.LoanStrategy;

import java.math.BigDecimal;

public final class AppConstants {

    private AppConstants() {
    }

    public static final int MAX_ACTIVE_LOANS = 3;
    public static final BigDecimal LOW_RISK_THRESHOLD = new BigDecimal("20.00");
    public static final BigDecimal HIGH_RISK_THRESHOLD = new BigDecimal("40.00");
    public static final int LONG_TERM_TENURE_THRESHOLD = 24;
    public static final String STRATEGY_FLAT_RATE = LoanStrategy.FLAT_RATE.name();
    public static final String STRATEGY_REDUCING_BALANCE = LoanStrategy.REDUCING_BALANCE.name();
    public static final String STRATEGY_STEP_UP = LoanStrategy.STEP_UP.name();
    public static final String STRATEGY_REJECTED = "REJECTED";
    public static final BigDecimal MAX_DB_DTI = new BigDecimal("999.99");
    public static final BigDecimal ANNUAL_STEP_UP_RATE = new BigDecimal("1.05");
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER = "Authorization";
    public static final int REMINDER_DAYS_BEFORE = 3;
}