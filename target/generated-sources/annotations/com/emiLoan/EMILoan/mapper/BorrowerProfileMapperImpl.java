package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T11:07:38+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class BorrowerProfileMapperImpl implements BorrowerProfileMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public BorrowerProfile toEntity(BorrowerRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        BorrowerProfile.BorrowerProfileBuilder borrowerProfile = BorrowerProfile.builder();

        borrowerProfile.monthlyIncome( request.getMonthlyIncome() );

        return borrowerProfile.build();
    }

    @Override
    public BorrowerResponse toResponse(BorrowerProfile profile) {
        if ( profile == null ) {
            return null;
        }

        BorrowerResponse.BorrowerResponseBuilder borrowerResponse = BorrowerResponse.builder();

        borrowerResponse.user( userMapper.toResponse( profile.getUser() ) );
        borrowerResponse.monthlyIncome( profile.getMonthlyIncome() );
        borrowerResponse.existingLoanCount( profile.getExistingLoanCount() );
        borrowerResponse.borrowerId( profile.getBorrowerId() );
        borrowerResponse.borrowerCode( profile.getBorrowerCode() );

        return borrowerResponse.build();
    }
}
