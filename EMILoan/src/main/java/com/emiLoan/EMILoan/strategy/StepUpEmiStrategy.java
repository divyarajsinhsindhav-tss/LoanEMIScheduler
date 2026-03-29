package com.emiLoan.EMILoan.strategy;


import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.emiLoan.EMILoan.common.constants.AppConstants.ANNUAL_STEP_UP_RATE;

@Component("STEP_UP")
public class StepUpEmiStrategy implements EmiCalculationStrategy {


    @Override
    public List<EmiRowData> generateSchedule(BigDecimal principal, BigDecimal annualRate, int months, LocalDate startDate) {
        List<EmiRowData> schedule = new ArrayList<>(months);

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal initialEmi = calculateInitialEmi(principal, monthlyRate, months);

        BigDecimal remainingBalance = principal;
        BigDecimal currentEmi = initialEmi;

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);

            if (i > 1 && (i - 1) % 12 == 0) {
                currentEmi = currentEmi.multiply(ANNUAL_STEP_UP_RATE).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComponent = currentEmi.subtract(interestComponent);

            if (i == months) {
                principalComponent = remainingBalance;
                currentEmi = principalComponent.add(interestComponent);
                remainingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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
        return "STEP_UP";
    }

    private BigDecimal calculateInitialEmi(BigDecimal principal, BigDecimal monthlyRate, int months) {
        BigDecimal low = BigDecimal.ZERO;

        BigDecimal high = principal.multiply(BigDecimal.ONE.add(monthlyRate.multiply(BigDecimal.valueOf(months))));
        BigDecimal mid = BigDecimal.ZERO;

        for (int i = 0; i < 100; i++) {
            mid = low.add(high).divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);

            BigDecimal balance = simulateLoan(principal, monthlyRate, months, mid);

            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return mid.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal simulateLoan(BigDecimal principal, BigDecimal monthlyRate, int months, BigDecimal initialEmi) {
        BigDecimal balance = principal;
        BigDecimal currentEmi = initialEmi;

        for (int i = 1; i <= months; i++) {
            if (i > 1 && (i - 1) % 12 == 0) {
                currentEmi = currentEmi.multiply(ANNUAL_STEP_UP_RATE).setScale(2, RoundingMode.HALF_UP);
            }
            BigDecimal interest = balance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComp = currentEmi.subtract(interest);
            balance = balance.subtract(principalComp);
        }

        return balance;
    }
}