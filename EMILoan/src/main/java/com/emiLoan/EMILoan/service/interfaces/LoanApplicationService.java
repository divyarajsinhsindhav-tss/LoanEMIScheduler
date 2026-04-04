package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationSubmitResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationWithdrawResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface LoanApplicationService {

    LoanApplicationSubmitResponse apply(LoanApplicationRequest request, String email);

    LoanApplicationDetailsResponse getByApplicationCode(String applicationCode);

    LoanApplicationResponse getApplication(String applicationCode, String email);

    Page<LoanApplicationDetailsResponse> getApplications(
            String email,
            Integer pageNumber,
            Integer pageSize,
            ApplicationStatus status
    );

    LoanApplicationWithdrawResponse withdrawApplication(String applicationCode, String email);


}