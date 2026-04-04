package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.loan.request.LoanStatusUpdateRequest;
import com.emiLoan.EMILoan.dto.loan.response.LoanResponse;
import com.emiLoan.EMILoan.dto.loan.response.LoanSummaryResponse;
import com.emiLoan.EMILoan.entity.Loan;
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
public class LoanMapperImpl implements LoanMapper {

    @Override
    public LoanResponse toResponse(Loan loan) {
        if ( loan == null ) {
            return null;
        }

        LoanResponse.LoanResponseBuilder loanResponse = LoanResponse.builder();

        loanResponse.applicationId( loanApplicationApplicationId( loan ) );
        loanResponse.applicationCode( loanApplicationApplicationCode( loan ) );
        loanResponse.borrowerId( loanBorrowerUserId( loan ) );
        loanResponse.borrowerName( mapFullName( loan.getBorrower() ) );
        loanResponse.loanId( loan.getLoanId() );
        loanResponse.loanCode( loan.getLoanCode() );
        loanResponse.principalAmount( loan.getPrincipalAmount() );
        loanResponse.interestRate( loan.getInterestRate() );
        loanResponse.tenureMonths( loan.getTenureMonths() );
        loanResponse.strategy( loan.getStrategy() );
        loanResponse.emiAmount( loan.getEmiAmount() );
        loanResponse.startDate( loan.getStartDate() );
        loanResponse.endDate( loan.getEndDate() );
        loanResponse.loanStatus( loan.getLoanStatus() );

        return loanResponse.build();
    }

    @Override
    public LoanSummaryResponse toSummaryResponse(Loan loan) {
        if ( loan == null ) {
            return null;
        }

        LoanSummaryResponse loanSummaryResponse = new LoanSummaryResponse();

        loanSummaryResponse.setLoanCode( loan.getLoanCode() );
        loanSummaryResponse.setPrincipalAmount( loan.getPrincipalAmount() );
        loanSummaryResponse.setEmiAmount( loan.getEmiAmount() );
        loanSummaryResponse.setLoanStatus( loan.getLoanStatus() );

        return loanSummaryResponse;
    }

    @Override
    public void updateEntityFromStatusRequest(LoanStatusUpdateRequest request, Loan loan) {
        if ( request == null ) {
            return;
        }

        loan.setLoanStatus( request.getLoanStatus() );
    }

    private UUID loanApplicationApplicationId(Loan loan) {
        LoanApplication application = loan.getApplication();
        if ( application == null ) {
            return null;
        }
        return application.getApplicationId();
    }

    private String loanApplicationApplicationCode(Loan loan) {
        LoanApplication application = loan.getApplication();
        if ( application == null ) {
            return null;
        }
        return application.getApplicationCode();
    }

    private UUID loanBorrowerUserId(Loan loan) {
        User borrower = loan.getBorrower();
        if ( borrower == null ) {
            return null;
        }
        return borrower.getUserId();
    }
}
