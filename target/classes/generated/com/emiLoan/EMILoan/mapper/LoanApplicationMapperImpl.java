package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.request.OfficerDecisionRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-30T15:57:30+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class LoanApplicationMapperImpl implements LoanApplicationMapper {

    @Override
    public LoanApplication toEntity(LoanApplicationRequest request) {
        if ( request == null ) {
            return null;
        }

        LoanApplication.LoanApplicationBuilder loanApplication = LoanApplication.builder();

        loanApplication.requestedAmount( request.getRequestedAmount() );
        loanApplication.tenureMonths( request.getTenureMonths() );
        loanApplication.existingEmi( request.getExistingEmi() );

        return loanApplication.build();
    }

    @Override
    public LoanApplicationResponse toResponse(LoanApplication application) {
        if ( application == null ) {
            return null;
        }

        LoanApplicationResponse.LoanApplicationResponseBuilder loanApplicationResponse = LoanApplicationResponse.builder();

        loanApplicationResponse.borrowerId( applicationBorrowerUserId( application ) );
        loanApplicationResponse.borrowerName( mapFullName( application.getBorrower() ) );
        loanApplicationResponse.reviewedByOfficerName( mapFullName( application.getReviewedBy() ) );
        loanApplicationResponse.applicationId( application.getApplicationId() );
        loanApplicationResponse.applicationCode( application.getApplicationCode() );
        loanApplicationResponse.requestedAmount( application.getRequestedAmount() );
        loanApplicationResponse.interestRate( application.getInterestRate() );
        loanApplicationResponse.tenureMonths( application.getTenureMonths() );
        loanApplicationResponse.dtiRatio( application.getDtiRatio() );
        loanApplicationResponse.suggestedStrategy( application.getSuggestedStrategy() );
        loanApplicationResponse.officerStrategy( application.getOfficerStrategy() );
        loanApplicationResponse.status( application.getStatus() );
        loanApplicationResponse.appliedAt( application.getAppliedAt() );
        loanApplicationResponse.reviewedAt( application.getReviewedAt() );

        return loanApplicationResponse.build();
    }

    @Override
    public LoanApplicationDetailsResponse toDetailsResponse(LoanApplication application) {
        if ( application == null ) {
            return null;
        }

        LoanApplicationDetailsResponse.LoanApplicationDetailsResponseBuilder loanApplicationDetailsResponse = LoanApplicationDetailsResponse.builder();

        loanApplicationDetailsResponse.application( toResponse( application ) );

        return loanApplicationDetailsResponse.build();
    }

    @Override
    public void updateEntityFromDecision(OfficerDecisionRequest request, LoanApplication application) {
        if ( request == null ) {
            return;
        }

        application.setOfficerStrategy( request.getOfficerStrategy() );
        application.setStatus( request.getStatus() );
        application.setInterestRate( request.getInterestRate() );
    }

    private UUID applicationBorrowerUserId(LoanApplication loanApplication) {
        User borrower = loanApplication.getBorrower();
        if ( borrower == null ) {
            return null;
        }
        return borrower.getUserId();
    }
}
