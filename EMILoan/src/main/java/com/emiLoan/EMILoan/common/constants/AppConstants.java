package com.emiLoan.EMILoan.common.constants;

public final class AppConstants {

    private AppConstants() {
    }

    public static final int MAX_ACTIVE_LOANS = 3;
    public static final double LOW_RISK_DTI = 20.0;
    public static final double HIGH_RISK_DTI = 40.0;
    public static final int STEP_UP_THRESHOLD_MONTHS = 24;
    public static final double STEP_UP_ANNUAL_INCREASE = 0.05;
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER = "Authorization";
    public static final int REMINDER_DAYS_BEFORE = 3;
}