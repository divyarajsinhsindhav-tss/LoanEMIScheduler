package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-27T21:23:15+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class EmployeeProfileMapperImpl implements EmployeeProfileMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public EmployeeProfile toEntity(LoanOfficerRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        EmployeeProfile.EmployeeProfileBuilder employeeProfile = EmployeeProfile.builder();

        employeeProfile.joiningDate( request.getJoiningDate() );
        employeeProfile.salary( request.getSalary() );

        return employeeProfile.build();
    }

    @Override
    public LoanOfficerResponse toResponse(EmployeeProfile profile) {
        if ( profile == null ) {
            return null;
        }

        LoanOfficerResponse.LoanOfficerResponseBuilder loanOfficerResponse = LoanOfficerResponse.builder();

        loanOfficerResponse.user( userMapper.toResponse( profile.getUser() ) );
        loanOfficerResponse.employeeId( profile.getEmployeeId() );
        loanOfficerResponse.employeeCode( profile.getEmployeeCode() );
        loanOfficerResponse.joiningDate( profile.getJoiningDate() );
        loanOfficerResponse.salary( profile.getSalary() );
        loanOfficerResponse.isActive( profile.getIsActive() );

        return loanOfficerResponse.build();
    }
}
