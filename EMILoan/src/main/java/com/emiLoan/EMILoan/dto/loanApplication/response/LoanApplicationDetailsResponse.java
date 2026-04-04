package com.emiLoan.EMILoan.dto.loanApplication.response;


import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationDetailsResponse {
    private LoanApplicationResponse application;
    private BorrowerResponse borrowerProfile;
    private String panFirst3;
    private String panLast2;
}
