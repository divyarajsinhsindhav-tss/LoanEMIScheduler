package com.emiLoan.EMILoan.dto.loanApplication.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Requested amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum loan amount is 1000")
    private BigDecimal requestedAmount;

    @NotNull(message = "Tenure is required")
    @Min(value = 3, message = "Minimum tenure is 3 months")
    private Integer tenureMonths;

    @DecimalMin(value = "0.0", message = "Existing EMI cannot be negative")
    private BigDecimal existingEmi;
}