package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.role.RoleResponse;
import com.emiLoan.EMILoan.entity.Role;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-04T13:08:19+0530",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Amazon.com Inc.)"
)
@Component
public class RoleMapperImpl implements RoleMapper {

    @Override
    public RoleResponse toResponse(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleResponse.RoleResponseBuilder roleResponse = RoleResponse.builder();

        roleResponse.roleId( role.getRoleId() );
        roleResponse.roleName( role.getRoleName() );
        roleResponse.description( role.getDescription() );

        return roleResponse.build();
    }
}
