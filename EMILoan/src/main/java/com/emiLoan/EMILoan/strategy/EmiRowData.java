package com.emiLoan.EMILoan.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EmiRowData(
        Integer installmentNo,
        LocalDate dueDate,
        BigDecimal principal,
        BigDecimal interest,
        BigDecimal totalEmi,
        BigDecimal remainingBalance
) {}