package com.emiLoan.EMILoan.dto.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanOfficerResponse {
    private UserResponse user;
    private UUID employeeId;
    private String employeeCode;
    private LocalDate joiningDate;
    private BigDecimal salary;
    private Boolean isActive;
}
