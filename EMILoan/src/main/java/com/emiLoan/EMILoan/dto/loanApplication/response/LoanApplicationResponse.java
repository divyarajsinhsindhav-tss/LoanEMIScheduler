package com.emiLoan.EMILoan.dto.loanApplication.response;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationResponse {
    private UUID applicationId;
    private String applicationCode;

    private UUID borrowerId;
    private String borrowerName;

    private BigDecimal requestedAmount;
    private BigDecimal interestRate;
    private Integer tenureMonths;
    private BigDecimal dtiRatio;

    private String suggestedStrategy;
    private String officerStrategy;
    private ApplicationStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime reviewedAt;
    private String reviewedByOfficerName;
}