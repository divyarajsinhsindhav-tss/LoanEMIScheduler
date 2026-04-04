package com.emiLoan.EMILoan.dto.loanApplication.response;


import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationWithdrawResponse {

    private String applicationCode;

    private BigDecimal requestedAmount;

    private ApplicationStatus status;

    private String message;
}