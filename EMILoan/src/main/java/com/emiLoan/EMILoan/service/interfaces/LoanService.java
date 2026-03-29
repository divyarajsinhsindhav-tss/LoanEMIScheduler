package com.emiLoan.EMILoan.service.interfaces;


import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;

import java.util.List;
import java.util.UUID;

public interface LoanService {

    /**
     * Converts an APPROVED LoanApplication into an active Loan and generates the EMI Schedule.
     */
    LoanResponse createLoanFromApplication(UUID applicationId);

    /**
     * Retrieves all loans for the authenticated borrower.
     */
    List<LoanResponse> getMyLoans(String email);

    /**
     * Retrieves full details of a specific loan.
     */
    LoanResponse getLoanById(UUID loanId);

    /**
     * Retrieves a high-level summary of the loan, including the next EMI due date.
     */
    LoanSummaryResponse getLoanSummary(UUID loanId, String email);

    /**
     * Allows system/officers to update the status of the loan (e.g., CLOSED, DEFAULTED).
     */
    LoanResponse updateLoanStatus(UUID loanId, LoanStatusUpdateRequest request);

    LoanResponse processDecision(UUID appId, OfficerDecisionRequest request, String officerEmail);
}