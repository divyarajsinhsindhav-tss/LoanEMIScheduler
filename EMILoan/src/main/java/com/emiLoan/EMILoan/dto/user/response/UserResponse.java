package com.emiLoan.EMILoan.dto.user.response;

import com.emiLoan.EMILoan.dto.role.RoleResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String userCode;
    private String personCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean isActive;
    private RoleResponse role;
    private String pan;
    private LocalDateTime createdAt;
}