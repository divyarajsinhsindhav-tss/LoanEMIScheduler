package com.emiLoan.EMILoan.strategy.EMI;



import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.emiLoan.EMILoan.common.constants.AppConstants.*;

@Component("FLAT_RATE")
public class FlatRateStrategy implements EmiCalculationStrategy {

    /**
     * Generates a loan schedule using the Flat Rate method.
     * 
     * MATH FORMULAS:
     * 1. Monthly Interest Rate (r) = Annual Rate / 1200
     * 2. Total Interest = Principal * r * Total Months
     * 3. Total Payable = Principal + Total Interest
     * 4. Fixed EMI = Total Payable / Total Months
     * 5. Monthly Interest Component = Total Interest / Total Months (Fixed every month)
     * 6. Monthly Principal Component = EMI - Monthly Interest Component (Fixed every month)
     *
     * WORKFLOW:
     * 1. Calculate monthly interest rate from annual rate.
     * 2. Calculate the total interest for the entire loan tenure upfront.
     * 3. Determine the fixed EMI by dividing (Principal + Total Interest) by months.
     * 4. Distribute the total interest equally across all months.
     * 5. Iterate through each month to build the schedule, adjusting the final month for rounding.
     */
    @Override
    public List<EmiRowData> generateSchedule(BigDecimal principal, BigDecimal annualRate, int months, LocalDate startDate) {
        List<EmiRowData> schedule = new ArrayList<>();

        // 1. Calculate Monthly Rate (r)
        BigDecimal monthlyRate = annualRate.divide(DIVISOR_1200, INTERNAL_PRECISION, RoundingMode.HALF_UP);

        // 2. Total Interest = P * r * n
        BigDecimal totalInterest = principal.multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);

        // 3. Fixed EMI = (Principal + Total Interest) / n
        BigDecimal fixedEmi = principal.add(totalInterest)
                .divide(BigDecimal.valueOf(months), CURRENCY_PRECISION, RoundingMode.HALF_UP);

        // 4. Equal distribution of Interest and Principal
        BigDecimal monthlyInterest = totalInterest.divide(BigDecimal.valueOf(months), CURRENCY_PRECISION, RoundingMode.HALF_UP);
        BigDecimal monthlyPrincipal = fixedEmi.subtract(monthlyInterest);

        BigDecimal remainingBalance = principal;

        // 5. Generate Monthly Rows
        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);

            BigDecimal currentPrincipal = monthlyPrincipal;
            BigDecimal currentInterest = monthlyInterest;
            BigDecimal currentEmi = fixedEmi;

            // Adjust the last installment to ensure balance hits exactly zero
            if (i == months) {
                currentPrincipal = remainingBalance;
                currentEmi = currentPrincipal.add(currentInterest);
                remainingBalance = BigDecimal.ZERO.setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);
            } else {
                remainingBalance = remainingBalance.subtract(currentPrincipal);
            }

            schedule.add(new EmiRowData(
                    i,
                    dueDate,
                    currentPrincipal,
                    currentInterest,
                    currentEmi,
                    remainingBalance
            ));
        }

        return schedule;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_FLAT_RATE;
    }
}