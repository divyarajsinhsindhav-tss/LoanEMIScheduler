package com.emiLoan.EMILoan.dto.role;

import com.emiLoan.EMILoan.common.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private UUID roleId;
    private RoleName roleName;
    private String description;
}