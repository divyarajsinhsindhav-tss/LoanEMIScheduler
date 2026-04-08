package com.emiLoan.EMILoan.dto.user.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BorrowerResponse {
    private UserResponse user;
    private String borrowerCode;
    private BigDecimal monthlyIncome;
    private Integer existingLoanCount;
}
