package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.UserRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.UserResponse;
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
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "userCode", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "monthly_income", source = "monthlyIncome")
    User toEntity(UserRegistrationRequest request);
    @Mapping(target = "monthlyIncome", source = "monthly_income")
    UserResponse toResponse(User user);
}