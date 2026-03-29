package com.emiLoan.EMILoan.engine;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.emiLoan.EMILoan.common.constants.AppConstants.MAX_DB_DTI;

@Component
public class DtiCalculationEngine {

    public BigDecimal calculate(BigDecimal existingEmi, BigDecimal monthlyIncome) {

        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly income must be greater than zero to calculate DTI.");
        }

        if (existingEmi == null || existingEmi.compareTo(BigDecimal.ZERO) < 0) {
            existingEmi = BigDecimal.ZERO;
        }

        BigDecimal dti = existingEmi.multiply(new BigDecimal("100"))
                .divide(monthlyIncome, 2, RoundingMode.HALF_UP);

        if (dti.compareTo(MAX_DB_DTI) > 0) {
            return MAX_DB_DTI;
        }

        return dti;
    }
}