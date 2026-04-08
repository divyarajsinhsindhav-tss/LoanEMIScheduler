package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationSubmitResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationWithdrawResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        imports = { com.emiLoan.EMILoan.common.enums.ApplicationStatus.class }
)
public interface LoanApplicationMapper {

    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "applicationCode", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "dtiRatio", ignore = true)
    LoanApplication toEntity(LoanApplicationRequest request);

    @Mapping(target = "borrowerName", source = "borrower", qualifiedByName = "mapFullName")
    @Mapping(target = "reviewedByOfficerName", source = "reviewedBy", qualifiedByName = "mapFullName")
    LoanApplicationResponse toResponse(LoanApplication application);

    LoanApplicationDetailsResponse toDetailsResponse(LoanApplication application);

    @Mapping(target = "message", expression = "java(application.getStatus() == ApplicationStatus.REJECTED ? \"We're sorry, your application did not meet our current lending criteria.\" : \"Your application has been received and is under review.\")")
    LoanApplicationSubmitResponse toSubmitResponse(LoanApplication application);

    @Mapping(target = "message", constant = "Your loan application has been successfully withdrawn.")
    LoanApplicationWithdrawResponse toWithdrawResponse(LoanApplication application);

    @Named("mapFullName")
    default String mapFullName(User user) {
        if (user == null) return null;
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }
}