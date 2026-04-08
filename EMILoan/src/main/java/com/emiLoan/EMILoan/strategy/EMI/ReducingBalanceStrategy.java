package com.emiLoan.EMILoan.strategy.EMI;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.emiLoan.EMILoan.common.constants.AppConstants.*;

@Component("REDUCING_BALANCE")
public class ReducingBalanceStrategy implements EmiCalculationStrategy {

    /**
     * Generates a loan schedule using the Reducing Balance method.
     * 
     * MATH FORMULAS:
     * 1. Monthly Interest Rate (r) = Annual Rate / 1200
     * 2. EMI = [P * r * (1 + r)^n] / [(1 + r)^n - 1]
     *    where:
     *    P = Principal amount
     *    r = Monthly interest rate
     *    n = Loan tenure in months
     * 3. Interest Component = Remaining Balance * r
     * 4. Principal Component = EMI - Interest Component
     *
     * WORKFLOW:
     * 1. Calculate the monthly interest rate (r).
     * 2. Calculate the monthly EMI using the standard amortized formula.
     * 3. For each month:
     *    a. Calculate interest on the *current* outstanding balance.
     *    b. Subtract interest from the EMI to get the principal repayment.
     *    c. Reduce the outstanding balance by that principal amount.
     * 4. Adjust the final month for any minor rounding differences.
     */
    @Override
    public List<EmiRowData> generateSchedule(
            final BigDecimal principal,
            final BigDecimal annualRate,
            final int months,
            final LocalDate startDate
    ) {
        final List<EmiRowData> schedule = new ArrayList<>(months);

        // Handle zero interest case
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(principal, months, startDate, schedule);
        }

        // 1. Calculate Monthly Interest Rate (r)
        final BigDecimal monthlyRate = annualRate.divide(DIVISOR_1200,INTERNAL_PRECISION, RoundingMode.HALF_UP);
        
        // 2. Calculate EMI using formula: [P * r * (1+r)^n] / [((1+r)^n) - 1]
        final BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        final BigDecimal compoundFactor = onePlusR.pow(months);

        final BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundFactor);
        final BigDecimal denominator = compoundFactor.subtract(BigDecimal.ONE);

        final BigDecimal standardEmi = numerator.divide(denominator, CURRENCY_PRECISION, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        // 3. Generate Monthly Rows
        for (int i = 1; i <= months; i++) {
            final LocalDate dueDate = startDate.plusMonths(i);

            // Interest component is calculated on the reducing balance
            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate)
                    .setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);

            BigDecimal principalComponent = standardEmi.subtract(interestComponent);
            BigDecimal currentEmi = standardEmi;

            // Final month adjustment
            if (i == months) {
                principalComponent = remainingBalance;
                currentEmi = principalComponent.add(interestComponent);
                remainingBalance = BigDecimal.ZERO.setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);
            } else {
                remainingBalance = remainingBalance.subtract(principalComponent);
            }

            schedule.add(new EmiRowData(
                    i,
                    dueDate,
                    principalComponent,
                    interestComponent,
                    currentEmi,
                    remainingBalance
            ));
        }

        return schedule;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_REDUCING_BALANCE;
    }

    private List<EmiRowData> generateZeroInterestSchedule(
            BigDecimal principal,
            int months,
            LocalDate startDate,
            List<EmiRowData> schedule
    ) {
        final BigDecimal emi = principal.divide(BigDecimal.valueOf(months), CURRENCY_PRECISION, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            final LocalDate dueDate = startDate.plusMonths(i);
            BigDecimal currentPrincipal = emi;

            if (i == months) {
                currentPrincipal = remainingBalance;
                remainingBalance = BigDecimal.ZERO.setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);
            } else {
                remainingBalance = remainingBalance.subtract(currentPrincipal);
            }

            schedule.add(new EmiRowData(i, dueDate, currentPrincipal, BigDecimal.ZERO, currentPrincipal, remainingBalance));
        }
        return schedule;
    }
}