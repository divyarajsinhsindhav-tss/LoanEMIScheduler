package com.emiLoan.EMILoan.mapper;


import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    @Mapping(target = "applicationId", source = "application.applicationId")
    @Mapping(target = "applicationCode", source = "application.applicationCode")
    LoanResponse toResponse(Loan loan);

    @Mapping(target = "nextDueDate", ignore = true)
    LoanSummaryResponse toSummaryResponse(Loan loan);

    @Mapping(target = "loanId", ignore = true)
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    void updateEntityFromStatusRequest(LoanStatusUpdateRequest request, @MappingTarget Loan loan);
}