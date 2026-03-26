package com.emiLoan.EMILoan.mapper;


import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanApplicationMapper {
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "applicationCode", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "dtiRatio", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);
    @Mapping(target = "borrowerId", source = "borrower.userId")
    @Mapping(target = "borrowerName", expression = "java(application.getBorrower().getFirstName() + \" \" + application.getBorrower().getLastName())")
    @Mapping(target = "reviewedByOfficerName", expression = "java(application.getReviewedBy() != null ? application.getReviewedBy().getFirstName() + \" \" + application.getReviewedBy().getLastName() : null)")
    LoanApplicationResponse toResponse(LoanApplication application);
    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    @Mapping(target = "officerStrategy", source = "officerStrategy")
    @Mapping(target = "status", source = "status")
    void updateEntityFromDecision(OfficerDecisionRequest request, @MappingTarget LoanApplication application);
}