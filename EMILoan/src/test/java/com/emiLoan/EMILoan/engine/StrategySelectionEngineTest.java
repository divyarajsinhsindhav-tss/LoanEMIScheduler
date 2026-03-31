package com.emiLoan.EMILoan.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.emiLoan.EMILoan.common.constants.AppConstants.*;
import static org.junit.jupiter.api.Assertions.*;

class StrategySelectionEngineTest {

    private StrategySelectionEngine engine;

    @BeforeEach
    void setup() {
        engine = new StrategySelectionEngine();
    }

    // LOW RISK → FLAT RATE
    @Test
    void suggest_shouldReturnFlatRate_whenDtiBelowLowRisk() {
        BigDecimal dti = LOW_RISK_THRESHOLD.subtract(new BigDecimal("1"));

        String result = engine.suggest(dti, 12);

        assertEquals(STRATEGY_FLAT_RATE, result);
    }

    // MID RISK + SHORT TENURE → REDUCING BALANCE
    @Test
    void suggest_shouldReturnReducingBalance_whenMidRiskAndShortTenure() {
        BigDecimal dti = LOW_RISK_THRESHOLD.add(new BigDecimal("1"));

        String result = engine.suggest(dti, LONG_TERM_TENURE_THRESHOLD - 1);

        assertEquals(STRATEGY_REDUCING_BALANCE, result);
    }

    // MID RISK + LONG TENURE → STEP UP
    @Test
    void suggest_shouldReturnStepUp_whenMidRiskAndLongTenure() {
        BigDecimal dti = LOW_RISK_THRESHOLD.add(new BigDecimal("1"));

        String result = engine.suggest(dti, LONG_TERM_TENURE_THRESHOLD);

        assertEquals(STRATEGY_STEP_UP, result);
    }

    // EXACT HIGH RISK THRESHOLD (boundary case)
    @Test
    void suggest_shouldHandleBoundaryAtHighRiskThreshold() {
        BigDecimal dti = HIGH_RISK_THRESHOLD;

        String result = engine.suggest(dti, LONG_TERM_TENURE_THRESHOLD);

        assertEquals(STRATEGY_STEP_UP, result);
    }

    // HIGH RISK → REJECTED
    @Test
    void suggest_shouldReturnRejected_whenDtiAboveHighRisk() {
        BigDecimal dti = HIGH_RISK_THRESHOLD.add(new BigDecimal("1"));

        String result = engine.suggest(dti, 12);

        assertEquals(STRATEGY_REJECTED, result);
    }

    // NULL DTI
    @Test
    void suggest_shouldThrowException_whenDtiIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.suggest(null, 12));
    }

    // INVALID TENURE
    @Test
    void suggest_shouldThrowException_whenTenureIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.suggest(new BigDecimal("10"), 0));
    }

    // NEGATIVE TENURE
    @Test
    void suggest_shouldThrowException_whenTenureIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.suggest(new BigDecimal("10"), -5));
    }
}