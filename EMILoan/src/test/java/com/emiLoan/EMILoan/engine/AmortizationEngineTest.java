package com.emiLoan.EMILoan.engine;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import com.emiLoan.EMILoan.entity.EmiSchedule;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.strategy.EmiCalculationStrategy;
import com.emiLoan.EMILoan.strategy.EmiRowData;
import com.emiLoan.EMILoan.strategy.EmiStrategyFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AmortizationEngineTest {

    @InjectMocks
    private AmortizationEngine engine;

    @Mock
    private EmiStrategyFactory strategyFactory;

    @Mock
    private EmiCalculationStrategy strategy;

    private Loan loan;

    @BeforeEach
    void setup() {
        loan = new Loan();
        loan.setPrincipalAmount(new BigDecimal("100000.0"));
        loan.setInterestRate(new BigDecimal("10.0"));
        loan.setTenureMonths(2);
        loan.setStartDate(LocalDate.now());
        loan.setStrategy("STANDARD");
    }

    // Success
    @Test
    void buildSchedule_shouldGenerateScheduleSuccessfully() {

        List<EmiRowData> rawRows = List.of(
                new EmiRowData(1, LocalDate.now().plusMonths(1),
                        new BigDecimal("4000"),
                        new BigDecimal("1000"),
                        new BigDecimal("5000"),
                        new BigDecimal("96000")),

                new EmiRowData(2, LocalDate.now().plusMonths(2),
                        new BigDecimal("4500"),
                        new BigDecimal("500"),
                        new BigDecimal("5000"),
                        new BigDecimal("0"))
        );

        when(strategyFactory.getStrategy("STANDARD")).thenReturn(strategy);

        when(strategy.generateSchedule(
                any(BigDecimal.class),
                any(BigDecimal.class),
                anyInt(),
                any(LocalDate.class)
        )).thenReturn(rawRows);

        when(strategy.getStrategyName()).thenReturn("STANDARD");

        List<EmiSchedule> result = engine.buildSchedule(loan);

        assertNotNull(result);
        assertEquals(2, result.size());

        EmiSchedule first = result.get(0);
        assertEquals(1, first.getInstallmentNo());
        assertEquals(new BigDecimal("4000"), first.getPrincipalComponent());
        assertEquals(new BigDecimal("1000"), first.getInterestComponent());
        assertEquals(EmiStatus.PENDING, first.getStatus());
    }

    // NULL LOAN
    @Test
    void buildSchedule_shouldThrowException_whenLoanIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                engine.buildSchedule(null));
    }

    // VERIFY STRATEGY CALL
    @Test
    void buildSchedule_shouldCallStrategyWithCorrectParams() {

        when(strategyFactory.getStrategy(any()))
                .thenReturn(strategy);

        when(strategy.generateSchedule(
                any(BigDecimal.class),
                any(BigDecimal.class),
                anyInt(),
                any(LocalDate.class)
        )).thenReturn(List.of());

        engine.buildSchedule(loan);

        verify(strategy).generateSchedule(
                eq(loan.getPrincipalAmount()),
                eq(loan.getInterestRate()),
                eq(loan.getTenureMonths()),
                eq(loan.getStartDate())
        );
    }
}