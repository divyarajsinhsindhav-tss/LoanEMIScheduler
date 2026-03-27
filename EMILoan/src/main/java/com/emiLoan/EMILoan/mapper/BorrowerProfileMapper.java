package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.user.request.BorrowerRegistrationRequest;
import com.emiLoan.EMILoan.dto.user.response.BorrowerResponse;
import com.emiLoan.EMILoan.entity.BorrowerProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface BorrowerProfileMapper {

    @Mapping(target = "borrowerId", ignore = true)
    @Mapping(target = "borrowerCode", ignore = true)
    @Mapping(target = "user", ignore = true)
    BorrowerProfile toEntity(BorrowerRegistrationRequest request);

    BorrowerResponse toResponse(BorrowerProfile profile);
}
