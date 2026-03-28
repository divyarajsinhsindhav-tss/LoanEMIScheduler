package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanApplicationMapper {

    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "applicationCode", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "interestRate", ignore = true)
    @Mapping(target = "dtiRatio", ignore = true)
        // REMOVED: createdAt and updatedAt ignores to stop Builder errors
    LoanApplication toEntity(LoanApplicationRequest request);

    @Mapping(target = "borrowerId", source = "borrower.userId")
    @Mapping(target = "borrowerName", source = "borrower", qualifiedByName = "mapFullName")
    @Mapping(target = "reviewedByOfficerName", source = "reviewedBy", qualifiedByName = "mapFullName")
    LoanApplicationResponse toResponse(LoanApplication application);

    @Mapping(target = "applicationId", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "appliedAt", ignore = true)
    @Mapping(target = "officerStrategy", source = "officerStrategy")
    @Mapping(target = "status", source = "status")
    void updateEntityFromDecision(OfficerDecisionRequest request, @MappingTarget LoanApplication application);

    @Named("mapFullName")
    default String mapFullName(User user) {
        if (user == null) return null;
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }
}