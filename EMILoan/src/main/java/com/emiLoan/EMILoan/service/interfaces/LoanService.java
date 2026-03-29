package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    LoanResponse createLoanFromApplication(UUID applicationId);

    List<LoanResponse> getMyLoans(String email);

    LoanResponse getLoanById(UUID loanId);

    LoanSummaryResponse getLoanSummary(UUID loanId, String email);

    LoanResponse updateLoanStatus(UUID loanId, LoanStatusUpdateRequest request);

    LoanResponse processDecision(String appId, OfficerDecisionRequest request);
}