package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.request.UserRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.RegistrationResponse;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
import com.emiLoan.EMILoan.dto.user.response.UserShortResponse;
import com.emiLoan.EMILoan.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {RoleMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface UserMapper {

    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    User toEntity(UserRegistrationRequest request);

    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(BorrowerRegistrationRequest request);

    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "person", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(LoanOfficerRegistrationRequest request);

    @Mapping(target = "personCode", source = "person.personCode")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "pan", expression = "java(user.getPerson() != null ? user.getPerson().getPanFirst3() + \"*****\" + user.getPerson().getPanLast2() : null)")
    UserResponse toResponse(User user);

    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getRoleName().name() : \"BORROWER\")")
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "verified", source = "isActive")
    RegistrationResponse toRegistrationResponse(User user);

    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().getRoleName().name() : null)")
    UserShortResponse toShort(User user);
}