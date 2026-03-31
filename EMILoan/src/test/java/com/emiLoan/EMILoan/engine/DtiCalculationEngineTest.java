package com.emiLoan.EMILoan.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.emiLoan.EMILoan.common.constants.AppConstants.MAX_DB_DTI;
import static org.junit.jupiter.api.Assertions.*;

class DtiCalculationEngineTest {

    private DtiCalculationEngine engine;

    @BeforeEach
    void setup() {
        engine = new DtiCalculationEngine();
    }

    // NORMAL CASE
    @Test
    void calculate_shouldReturnCorrectDti() {
        BigDecimal emi = new BigDecimal("20000");
        BigDecimal income = new BigDecimal("100000");

        BigDecimal result = engine.calculate(emi, income);

        assertEquals(new BigDecimal("20.00"), result);
    }

    // NULL EMI → should treat as ZERO
    @Test
    void calculate_shouldHandleNullEmi() {
        BigDecimal result = engine.calculate(null, new BigDecimal("100000"));

        assertEquals(new BigDecimal("0.00"), result);
    }

    // NEGATIVE EMI → should treat as ZERO
    @Test
    void calculate_shouldHandleNegativeEmi() {
        BigDecimal result = engine.calculate(new BigDecimal("-5000"), new BigDecimal("100000"));

        assertEquals(new BigDecimal("0.00"), result);
    }

    // NULL INCOME
    @Test
    void calculate_shouldThrowException_whenIncomeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculate(new BigDecimal("10000"), null));
    }

    // ZERO INCOME
    @Test
    void calculate_shouldThrowException_whenIncomeIsZero() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculate(new BigDecimal("10000"), BigDecimal.ZERO));
    }

    // NEGATIVE INCOME
    @Test
    void calculate_shouldThrowException_whenIncomeIsNegative() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.calculate(new BigDecimal("10000"), new BigDecimal("-1000")));
    }

    // MAX CAP APPLIED
    @Test
    void calculate_shouldCapAtMaxDti() {
        BigDecimal emi = new BigDecimal("90000");
        BigDecimal income = new BigDecimal("100000");

        BigDecimal result = engine.calculate(emi, income);

        assertEquals(MAX_DB_DTI, result);
    }

    // ROUNDING CHECK
    @Test
    void calculate_shouldRoundToTwoDecimalPlaces() {
        BigDecimal emi = new BigDecimal("33333");
        BigDecimal income = new BigDecimal("100000");

        BigDecimal result = engine.calculate(emi, income);

        assertEquals(new BigDecimal("33.33"), result);
    }
}