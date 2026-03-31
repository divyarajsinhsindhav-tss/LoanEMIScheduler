package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.entity.AuditLog;
import com.emiLoan.EMILoan.entity.StrategyAudit;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    LoanResponse createLoanFromApplication(UUID applicationId);

    List<LoanResponse> getMyLoans(String email);

    LoanResponse getLoan(String loanCode, String email);

    LoanSummaryResponse getLoanSummary(String loanCode, String email);

    LoanResponse updateLoanStatus(UUID loanId, LoanStatusUpdateRequest request);

    LoanResponse processDecision(String applicationCode, OfficerDecisionRequest request,String officerEmail);

    List<StrategyAudit> getStrategyOverrides(String requesterEmail);

    List<AuditLog> getLoanAuditHistory(String loanCode, String requesterEmail);

    List<AuditLog> getApplicationAuditHistory(String applicationCode, String requesterEmail);
}