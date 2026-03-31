package com.emiLoan.EMILoan.strategy.EMI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface EmiCalculationStrategy {

    List<EmiRowData> generateSchedule(
            BigDecimal principal,
            BigDecimal annualRate,
            int months,
            LocalDate startDate
    );

    String getStrategyName();
}
