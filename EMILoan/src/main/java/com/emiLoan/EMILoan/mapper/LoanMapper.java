package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.entity.Loan;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoanMapper {

    @Mapping(target = "applicationId", source = "application.applicationId")
    @Mapping(target = "applicationCode", source = "application.applicationCode")
    @Mapping(target = "borrowerId", source = "borrower.userId")
    @Mapping(target = "borrowerName", source = "borrower", qualifiedByName = "mapFullName")
    LoanResponse toResponse(Loan loan);

    @Mapping(target = "nextDueDate", ignore = true)
    LoanSummaryResponse toSummaryResponse(Loan loan);

    @Mapping(target = "loanId", ignore = true)
    @Mapping(target = "application", ignore = true)
    @Mapping(target = "borrower", ignore = true)
    @Mapping(target = "loanCode", ignore = true)
    void updateEntityFromStatusRequest(LoanStatusUpdateRequest request, @MappingTarget Loan loan);


    @Named("mapFullName")
    default String mapFullName(User user) {
        if (user == null) return null;
        String first = user.getFirstName() != null ? user.getFirstName() : "";
        String last = user.getLastName() != null ? user.getLastName() : "";
        return (first + " " + last).trim();
    }
}