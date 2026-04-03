package com.emiLoan.EMILoan.dto.emiSchedule.response;

import com.emiLoan.EMILoan.common.enums.EmiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmiScheduleResponse {
    private UUID emiId;
    private Integer installmentNo;
    private LocalDate dueDate;

    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private BigDecimal totalEmi;
    private BigDecimal remainingBalance;

    private EmiStatus status;
    private LocalDate paidDate;

    private BigDecimal amountPaid;
    private BigDecimal amountDue;
}