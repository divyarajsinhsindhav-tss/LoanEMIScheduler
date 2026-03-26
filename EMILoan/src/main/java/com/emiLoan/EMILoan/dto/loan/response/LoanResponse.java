package com.emiLoan.EMILoan.dto.loan.response;


import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.common.enums.LoanStrategy;
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

    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private LoanStrategy strategy;
    private BigDecimal emiAmount;

    private LocalDate startDate;
    private LocalDate endDate;
    private LoanStatus loanStatus;
}