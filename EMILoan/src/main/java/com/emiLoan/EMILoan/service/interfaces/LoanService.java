package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.common.enums.LoanStatus;
import com.emiLoan.EMILoan.dto.auditLogs.AuditLogResponse;
import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.strategyAudit.StrategyAuditResponse;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.StrategyAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface LoanService {


    LoanResponse getLoan(String loanCode, String email);

    LoanSummaryResponse getLoanSummary(String loanCode, String email);

    LoanResponse updateLoanStatus(String loanCode, LoanStatusUpdateRequest request);

    LoanResponse processDecision(String applicationCode, OfficerDecisionRequest request, String officerEmail);

    Page<AuditLogResponse> getLoanAuditHistory(String loanCode, String requesterEmail,Pageable pageable);

    Page<LoanResponse> getAllLoans(String requesterEmail, int pageNumber, int pageSize, LoanStatus status);

    Page<StrategyAuditResponse> getStrategyOverrides(String requesterEmail,Pageable pageable);

    Page<LoanResponse> getMyLoans(String email,Pageable pageable);
}