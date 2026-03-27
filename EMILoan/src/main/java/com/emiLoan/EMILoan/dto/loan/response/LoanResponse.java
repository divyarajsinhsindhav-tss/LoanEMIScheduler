package com.emiLoan.EMILoan.dto.loan.response;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
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
public class LoanResponse {
    private UUID loanId;
    private String loanCode;

    private UUID applicationId;
    private String applicationCode;

    private UUID borrowerId;
    private String borrowerName;

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private String strategy;
    private BigDecimal emiAmount;

    private LocalDate startDate;
    private LocalDate endDate;
    private LoanStatus loanStatus;
}