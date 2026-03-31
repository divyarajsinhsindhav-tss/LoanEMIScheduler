package com.emiLoan.EMILoan.strategy.EMI;


import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("FLAT_RATE")
public class FlatRateStrategy implements EmiCalculationStrategy {

    @Override
    public List<EmiRowData> generateSchedule(BigDecimal principal, BigDecimal annualRate, int months, LocalDate startDate) {
        List<EmiRowData> schedule = new ArrayList<>();

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal totalInterest = principal.multiply(monthlyRate)
                .multiply(BigDecimal.valueOf(months))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal fixedEmi = principal.add(totalInterest)
                .divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);

        BigDecimal monthlyInterest = totalInterest.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal monthlyPrincipal = fixedEmi.subtract(monthlyInterest);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);

            BigDecimal currentPrincipal = monthlyPrincipal;
            BigDecimal currentInterest = monthlyInterest;
            BigDecimal currentEmi = fixedEmi;

            if (i == months) {
                currentPrincipal = remainingBalance;
                currentEmi = currentPrincipal.add(currentInterest);
                remainingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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
        return "FLAT_RATE";
    }
}