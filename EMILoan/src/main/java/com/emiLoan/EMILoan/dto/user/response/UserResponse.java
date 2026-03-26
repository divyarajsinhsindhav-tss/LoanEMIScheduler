package com.emiLoan.EMILoan.dto.user.response;


import com.emiLoan.EMILoan.dto.role.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID userId;
    private String userCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private BigDecimal monthlyIncome;
    private Boolean enabled;
    private Set<RoleResponse> roles;
    private LocalDateTime createdAt;
}