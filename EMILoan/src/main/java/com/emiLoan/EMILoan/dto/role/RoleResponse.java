package com.emiLoan.EMILoan.dto.role;

import com.emiLoan.EMILoan.common.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Integer roleId;
    private RoleName roleName;
}