package com.emiLoan.EMILoan.strategy;

import com.emiLoan.EMILoan.strategy.EmiCalculationStrategy;
import com.emiLoan.EMILoan.strategy.EmiRowData;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("REDUCING_BALANCE")
public class ReducingBalanceStrategy implements EmiCalculationStrategy {

    @Override
    public List<EmiRowData> generateSchedule(BigDecimal principal, BigDecimal annualRate, int months, LocalDate startDate) {
        List<EmiRowData> schedule = new ArrayList<>(months);

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(principal, months, startDate, schedule);
        }

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal compoundFactor = onePlusR.pow(months);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundFactor);
        BigDecimal denominator = compoundFactor.subtract(BigDecimal.ONE);

        BigDecimal standardEmi = numerator.divide(denominator, 2, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);

            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal principalComponent = standardEmi.subtract(interestComponent);
            BigDecimal currentEmi = standardEmi;

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
        return "REDUCING_BALANCE";
    }


    private List<EmiRowData> generateZeroInterestSchedule(BigDecimal principal, int months, LocalDate startDate, List<EmiRowData> schedule) {
        BigDecimal emi = principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            LocalDate dueDate = startDate.plusMonths(i);
            BigDecimal currentPrincipal = emi;
            BigDecimal currentEmi = emi;

            if (i == months) {
                currentPrincipal = remainingBalance;
                currentEmi = remainingBalance;
                remainingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            } else {
                remainingBalance = remainingBalance.subtract(currentPrincipal);
            }

            schedule.add(new EmiRowData(i, dueDate, currentPrincipal, BigDecimal.ZERO, currentEmi, remainingBalance));
        }
        return schedule;
    }
}