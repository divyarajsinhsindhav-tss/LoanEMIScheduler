package com.emiLoan.EMILoan.service.interfaces;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoanApplicationService {

    LoanApplicationResponse apply(LoanApplicationRequest request);

    LoanApplicationResponse getApplication(String applicationId);

    Page<LoanApplicationDetailsResponse> getApplications(Integer pageNumber, Integer pageSize, ApplicationStatus status);

}