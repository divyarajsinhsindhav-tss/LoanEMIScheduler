package com.emiLoan.EMILoan.engine;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

import static com.emiLoan.EMILoan.common.constants.AppConstants.*;

@Component
public class StrategySelectionEngine {

    public String suggest(BigDecimal dtiRatio, int tenureMonths) {
        if (dtiRatio == null) {
            throw new IllegalArgumentException("DTI Ratio must be provided for strategy selection.");
        }
        if (tenureMonths <= 0) {
            throw new IllegalArgumentException("Tenure months must be greater than zero.");
        }
        if (dtiRatio.compareTo(LOW_RISK_THRESHOLD) < 0) {
            return STRATEGY_FLAT_RATE;
        }
        else if (dtiRatio.compareTo(HIGH_RISK_THRESHOLD) <= 0) {
            if (tenureMonths < LONG_TERM_TENURE_THRESHOLD) {
                return STRATEGY_REDUCING_BALANCE;
            } else {
                return STRATEGY_STEP_UP;
            }
        }
        else {
            return STRATEGY_REJECTED;
        }
    }
}