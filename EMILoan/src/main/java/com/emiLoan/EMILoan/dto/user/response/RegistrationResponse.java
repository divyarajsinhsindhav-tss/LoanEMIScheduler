package com.emiLoan.EMILoan.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private UUID userId;
    private String userCode;
    private String email;
    private String message;
    private boolean verified;
    private String role;
}