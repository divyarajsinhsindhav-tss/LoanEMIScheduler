package com.emiLoan.EMILoan.dto.loanApplication.response;


import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationSubmitResponse {

    private String applicationCode;

    private BigDecimal requestedAmount;
    private Integer tenureMonths;

    private ApplicationStatus status;

    private LocalDateTime appliedAt;

    private String message;
}