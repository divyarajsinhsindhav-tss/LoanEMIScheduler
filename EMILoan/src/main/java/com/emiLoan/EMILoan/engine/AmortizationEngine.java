package com.emiLoan.EMILoan.engine;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.strategy.EMI.EmiCalculationStrategy;
import com.emiLoan.EMILoan.strategy.EMI.EmiStrategyFactory;
import com.emiLoan.EMILoan.strategy.EMI.EmiRowData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AmortizationEngine {

    private final EmiStrategyFactory emiStrategyFactory;

    public List<EmiSchedule> buildSchedule(Loan loan) {
        if (loan == null) {
            throw new IllegalArgumentException("Cannot build schedule: Loan entity is null.");
        }

        long startTime = System.currentTimeMillis();

        EmiCalculationStrategy strategy = emiStrategyFactory.getStrategy(loan.getStrategy());

        List<EmiRowData> rawSchedule = strategy.generateSchedule(
                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getStartDate()
        );

        List<EmiSchedule> scheduleEntities = new ArrayList<>(rawSchedule.size());

        for (EmiRowData row : rawSchedule) {
            EmiSchedule emiEntity = EmiSchedule.builder()
                    .loan(loan)
                    .installmentNo(row.installmentNo())
                    .dueDate(row.dueDate())
                    .principalComponent(row.principal())
                    .interestComponent(row.interest())
                    .totalEmi(row.totalEmi())
                    .remainingBalance(row.remainingBalance())
                    .status(EmiStatus.PENDING)
                    .build();

            scheduleEntities.add(emiEntity);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        log.info("Generated {}-month schedule using {} strategy in {}ms",
                loan.getTenureMonths(), strategy.getStrategyName(), executionTime);

        return scheduleEntities;
    }
}