package com.emiLoan.EMILoan.mapper;

import com.emiLoan.EMILoan.dto.role.RoleResponse;
import com.emiLoan.EMILoan.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {
   RoleResponse toResponse(Role role);
}