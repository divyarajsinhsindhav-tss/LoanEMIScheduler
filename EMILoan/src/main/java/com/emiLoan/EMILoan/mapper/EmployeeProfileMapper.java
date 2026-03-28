package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.LoanOfficerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.LoanOfficerResponse;
import com.emiLoan.EMILoan.entity.EmployeeProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;



@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface EmployeeProfileMapper {

    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "employeeCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    EmployeeProfile toEntity(LoanOfficerRegistrationRequest request);

    @Mapping(target = "user", source = "user")
    LoanOfficerResponse toResponse(EmployeeProfile profile);
}