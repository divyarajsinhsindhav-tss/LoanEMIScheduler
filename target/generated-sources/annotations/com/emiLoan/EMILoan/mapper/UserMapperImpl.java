package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.UserRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.RegistrationResponse;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.dto.user.response.UserShortResponse;
import com.emiLoan.EMILoan.entity.PersonIdentity;
import com.emiLoan.EMILoan.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-08T11:07:38+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public User toEntity(UserRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.phone( request.getPhone() );

        return user.build();
    }

    @Override
    public User toEntity(BorrowerRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.phone( request.getPhone() );

        return user.build();
    }

    @Override
    public User toEntity(LoanOfficerRegistrationRequest request) {
        if ( request == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.firstName( request.getFirstName() );
        user.lastName( request.getLastName() );
        user.email( request.getEmail() );
        user.phone( request.getPhone() );

        return user.build();
    }

    @Override
    public UserResponse toResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse.UserResponseBuilder userResponse = UserResponse.builder();

        userResponse.personCode( userPersonPersonCode( user ) );
        userResponse.role( roleMapper.toResponse( user.getRole() ) );
        userResponse.userId( user.getUserId() );
        userResponse.userCode( user.getUserCode() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.email( user.getEmail() );
        userResponse.phone( user.getPhone() );
        userResponse.isActive( user.getIsActive() );
        userResponse.createdAt( user.getCreatedAt() );

        userResponse.pan( user.getPerson() != null ? user.getPerson().getPanFirst3() + "*****" + user.getPerson().getPanLast2() : null );

        return userResponse.build();
    }

    @Override
    public RegistrationResponse toRegistrationResponse(User user) {
        if ( user == null ) {
            return null;
        }

        RegistrationResponse.RegistrationResponseBuilder registrationResponse = RegistrationResponse.builder();

        if ( user.getIsActive() != null ) {
            registrationResponse.verified( user.getIsActive() );
        }
        registrationResponse.userId( user.getUserId() );
        registrationResponse.userCode( user.getUserCode() );
        registrationResponse.email( user.getEmail() );

        registrationResponse.role( user.getRole() != null ? user.getRole().getRoleName().name() : "BORROWER" );

        return registrationResponse.build();
    }

    @Override
    public UserShortResponse toShort(User user) {
        if ( user == null ) {
            return null;
        }

        UserShortResponse.UserShortResponseBuilder userShortResponse = UserShortResponse.builder();

        userShortResponse.userCode( user.getUserCode() );
        userShortResponse.firstName( user.getFirstName() );
        userShortResponse.lastName( user.getLastName() );

        userShortResponse.role( user.getRole() != null ? user.getRole().getRoleName().name() : null );

        return userShortResponse.build();
    }

    private String userPersonPersonCode(User user) {
        PersonIdentity person = user.getPerson();
        if ( person == null ) {
            return null;
        }
        return person.getPersonCode();
    }
}
