package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.common.enums.ApplicationStatus;
import com.emiLoan.EMILoan.dto.loanApplication.request.LoanApplicationRequest;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationDetailsResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationSubmitResponse;
import com.emiLoan.EMILoan.dto.loanApplication.response.LoanApplicationWithdrawResponse;
import com.emiLoan.EMILoan.entity.LoanApplication;
import com.emiLoan.EMILoan.entity.User;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-04T11:16:37+0530",
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
    public LoanApplicationSubmitResponse toSubmitResponse(LoanApplication application) {
        if ( application == null ) {
            return null;
        }

        LoanApplicationSubmitResponse.LoanApplicationSubmitResponseBuilder loanApplicationSubmitResponse = LoanApplicationSubmitResponse.builder();

        loanApplicationSubmitResponse.applicationCode( application.getApplicationCode() );
        loanApplicationSubmitResponse.requestedAmount( application.getRequestedAmount() );
        loanApplicationSubmitResponse.tenureMonths( application.getTenureMonths() );
        loanApplicationSubmitResponse.status( application.getStatus() );
        loanApplicationSubmitResponse.appliedAt( application.getAppliedAt() );

        loanApplicationSubmitResponse.message( application.getStatus() == ApplicationStatus.REJECTED ? "We're sorry, your application did not meet our current lending criteria." : "Your application has been received and is under review." );

        return loanApplicationSubmitResponse.build();
    }

    @Override
    public LoanApplicationWithdrawResponse toWithdrawResponse(LoanApplication application) {
        if ( application == null ) {
            return null;
        }

        LoanApplicationWithdrawResponse.LoanApplicationWithdrawResponseBuilder loanApplicationWithdrawResponse = LoanApplicationWithdrawResponse.builder();

        loanApplicationWithdrawResponse.applicationCode( application.getApplicationCode() );
        loanApplicationWithdrawResponse.requestedAmount( application.getRequestedAmount() );
        loanApplicationWithdrawResponse.status( application.getStatus() );

        loanApplicationWithdrawResponse.message( "Your loan application has been successfully withdrawn." );

        return loanApplicationWithdrawResponse.build();
    }

    private UUID applicationBorrowerUserId(LoanApplication loanApplication) {
        User borrower = loanApplication.getBorrower();
        if ( borrower == null ) {
            return null;
        }
        return borrower.getUserId();
    }
}
