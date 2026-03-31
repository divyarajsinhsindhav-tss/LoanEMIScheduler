package com.emiLoan.EMILoan.strategy.EMI;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("REDUCING_BALANCE")
public class ReducingBalanceStrategy implements EmiCalculationStrategy {

    private static final BigDecimal DIVISOR_1200 = BigDecimal.valueOf(1200);
    private static final int INTERNAL_PRECISION = 10;
    private static final int CURRENCY_PRECISION = 2;

    @Override
    public List<EmiRowData> generateSchedule(
            final BigDecimal principal,
            final BigDecimal annualRate,
            final int months,
            final LocalDate startDate
    ) {
        final List<EmiRowData> schedule = new ArrayList<>(months);

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return generateZeroInterestSchedule(principal, months, startDate, schedule);
        }

        final BigDecimal monthlyRate = annualRate.divide(DIVISOR_1200, INTERNAL_PRECISION, RoundingMode.HALF_UP);
        final BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        final BigDecimal compoundFactor = onePlusR.pow(months);

        final BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundFactor);
        final BigDecimal denominator = compoundFactor.subtract(BigDecimal.ONE);

        final BigDecimal standardEmi = numerator.divide(denominator, CURRENCY_PRECISION, RoundingMode.HALF_UP);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= months; i++) {
            final LocalDate dueDate = startDate.plusMonths(i);

            BigDecimal interestComponent = remainingBalance.multiply(monthlyRate)
                    .setScale(CURRENCY_PRECISION, RoundingMode.HALF_UP);

            BigDecimal principalComponent = standardEmi.subtract(interestComponent);
            BigDecimal currentEmi = standardEmi;

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
        return "REDUCING_BALANCE";
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