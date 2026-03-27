package com.emiLoan.EMILoan.dto.loanApplication.request;


import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OfficerDecisionRequest {

    @NotNull(message = "Decision status is required")
    private ApplicationStatus status;

    private String officerStrategy;

    private String remarks;
}