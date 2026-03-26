package com.emiLoan.EMILoan.dto.loan.request;

import com.emiLoan.EMILoan.common.enums.LoanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanStatusUpdateRequest {
    @NotNull(message = "New loan status is required")
    private LoanStatus loanStatus;
}