package com.emiLoan.EMILoan.dto.loan.response;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanResponse {
    private String loanCode;

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