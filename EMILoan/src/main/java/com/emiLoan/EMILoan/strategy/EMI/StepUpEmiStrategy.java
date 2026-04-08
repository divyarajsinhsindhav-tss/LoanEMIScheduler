package com.emiLoan.EMILoan.strategy.EMI;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.emiLoan.EMILoan.common.constants.AppConstants.*;

@Component("STEP_UP")
public class StepUpEmiStrategy implements EmiCalculationStrategy {

    /**
     * Generates a loan schedule where the EMI increases periodically (e.g., every
     * year).
     * 
     * MATH CONCEPT:
     * 1. Annual Step-Up: EMI increases by a fixed percentage (e.g., 5%) every 12
     * months.
     * 2. Finding Initial EMI: Since there is no simple closed-form formula for
     * Step-Up EMI
     * with reducing balance, we use a Numerical Method (Binary Search / Bisection).
     * 3. Goal: Find 'x' (Initial EMI) such that simulating the loan with
     * EMI(t) = x * (1 + StepUpRate)^floor(t/12) results in 0 balance at tenure end.
     *
     * WORKFLOW:
     * 1. Calculate the monthly interest rate.
     * 2. Use Binary Search (calculateInitialEmi) to find the starting EMI.
     * - It simulates the entire loan 100 times, narrowing down the EMI value.
     * 3. Once the initial EMI is found, iterate through each month:
     * - Every 12 months, increase the 'currentEmi' by the ANNUAL_STEP_UP_RATE.
     * - Calculate interest on the remaining balance.
     * - Calculate principal as (Current EMI - Interest).
     * - Update the remaining balance.
     * 4. Adjust the final month for rounding.
     */
    @Override
    public List<EmiRowData> generateSchedule(BigDecimal principal, BigDecimal annualRate, int months,
            LocalDate startDate) {
        List<EmiRowData> schedule = new ArrayList<>(months);

        // 1. Calculate Monthly Interest Rate
        BigDecimal monthlyRate = annualRate.divide(DIVISOR_1200, INTERNAL_PRECISION, RoundingMode.HALF_UP);

        // 2. Find the starting EMI using Binary Search/Bisection Method
        BigDecimal initialEmi = calculateInitialEmi(principal, monthlyRate, months);

        BigDecimal remainingBalance = principal;
        BigDecimal currentEmi = initialEmi;

        // 3. Generate Schedule
        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);

            // Step up the EMI every 12 months (Annual increase)
            if (i > 1 && (i - 1) % 12 == 0) {
                currentEmi = currentEmi.multiply(ANNUAL_STEP_UP_RATE).setScale(CURRENCY_PRECISION,
                        RoundingMode.HALF_UP);
            }

            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate).setScale(CURRENCY_PRECISION,
                    RoundingMode.HALF_UP);
            BigDecimal principalComponent = currentEmi.subtract(interestComponent);

            // Final installment adjustment
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
                    remainingBalance));
        }

        return schedule;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_STEP_UP;
    }

    /**
     * Calculates the starting EMI using the Binary Search (Bisection) method.
     *
     * Since the Step-Up strategy involves an increasing EMI on a reducing balance,
     * it is mathematically difficult to solve with a direct formula.
     * Instead, we search for the 'Initial EMI' within a range [0,
     * Max_Possible_EMI].
     *
     * BINARY SEARCH STEPS:
     * 1. Define 'low' as 0 and 'high' as the total potential loan cost.
     * 2. Iterate 100 times to achieve extreme precision (well beyond currency
     * decimals).
     * 3. Pick a 'mid' value as a candidate for the Initial EMI.
     * 4. Call {@link #simulateLoan} to see if 'mid' pays off the loan in full.
     * 5. If simulation results in a positive balance, 'mid' is too low (increase
     * low).
     * 6. If simulation results in a negative balance, 'mid' is too high (decrease
     * high).
     */
    private BigDecimal calculateInitialEmi(BigDecimal principal, BigDecimal monthlyRate, int months) {
        BigDecimal low = BigDecimal.ZERO;

        BigDecimal high = principal.multiply(BigDecimal.ONE.add(monthlyRate.multiply(BigDecimal.valueOf(months))));
        BigDecimal mid = BigDecimal.ZERO;

        for (int i = 0; i < 100; i++) {
            mid = low.add(high).divide(BigDecimal.valueOf(2), INTERNAL_PRECISION, RoundingMode.HALF_UP);

            BigDecimal balance = simulateLoan(principal, monthlyRate, months, mid);

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return mid.setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);
    }

    private BigDecimal simulateLoan(BigDecimal principal, BigDecimal monthlyRate, int months, BigDecimal initialEmi) {
        BigDecimal balance = principal;
        BigDecimal currentEmi = initialEmi;

        for (int i = 1; i <= months; i++) {
            if (i > 1 && (i - 1) % 12 == 0) {
                currentEmi = currentEmi.multiply(ANNUAL_STEP_UP_RATE).setScale(CURRENCY_PRECISION,
                        RoundingMode.HALF_UP);
            }
            BigDecimal interest = balance.multiply(monthlyRate).setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);
            BigDecimal principalComp = currentEmi.subtract(interest);
            balance = balance.subtract(principalComp);
        }

        return balance;
    }
}