package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanApplicationService {

    LoanApplicationResponse apply(LoanApplicationRequest request, String email);

    Page<LoanApplicationResponse> getMyApplications(String email, int page, int size);

    Page<LoanApplicationResponse> getAllPending(int page, int size);

    LoanApplicationDetailsResponse getById(UUID applicationId);

}