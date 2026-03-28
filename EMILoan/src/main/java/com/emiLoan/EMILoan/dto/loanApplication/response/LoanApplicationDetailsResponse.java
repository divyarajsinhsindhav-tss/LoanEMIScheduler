package com.emiLoan.EMILoan.dto.loanApplication.response;


import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoanApplicationDetailsResponse {
    private LoanApplicationResponse application;
    private BorrowerResponse borrowerProfile;
    private String panFirst3;
    private String panLast2;
}
