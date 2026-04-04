package com.emiLoan.EMILoan.dto.loan.response;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanSummaryResponse {
    private String loanCode;
    private BigDecimal principalAmount;
    private BigDecimal emiAmount;
    private LocalDate nextDueDate;
    private LoanStatus loanStatus;
}
