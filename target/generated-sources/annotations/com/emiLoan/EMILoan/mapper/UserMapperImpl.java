package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.role.RoleResponse;
import com.emiLoan.EMILoan.dto.user.request.UserRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.entity.Role;
import com.emiLoan.EMILoan.entity.User;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-26T17:44:33+0530",
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

        user.monthly_income( request.getMonthlyIncome() );
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

        userResponse.monthlyIncome( user.getMonthly_income() );
        userResponse.userId( user.getUserId() );
        userResponse.userCode( user.getUserCode() );
        userResponse.firstName( user.getFirstName() );
        userResponse.lastName( user.getLastName() );
        userResponse.email( user.getEmail() );
        userResponse.phone( user.getPhone() );
        userResponse.enabled( user.getEnabled() );
        userResponse.roles( roleSetToRoleResponseSet( user.getRoles() ) );
        userResponse.createdAt( user.getCreatedAt() );

        return userResponse.build();
    }

    protected Set<RoleResponse> roleSetToRoleResponseSet(Set<Role> set) {
        if ( set == null ) {
            return null;
        }

        Set<RoleResponse> set1 = LinkedHashSet.newLinkedHashSet( set.size() );
        for ( Role role : set ) {
            set1.add( roleMapper.toResponse( role ) );
        }

        return set1;
    }
}
