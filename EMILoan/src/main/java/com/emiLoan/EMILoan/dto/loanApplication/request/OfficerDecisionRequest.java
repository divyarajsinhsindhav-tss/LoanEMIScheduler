package com.emiLoan.EMILoan.dto.loanApplication.request;


import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OfficerDecisionRequest {

    @NotNull(message = "Decision status is required")
    private ApplicationStatus status;
    private String officerStrategy;
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    private BigDecimal interestRate;
    private String remarks;
}