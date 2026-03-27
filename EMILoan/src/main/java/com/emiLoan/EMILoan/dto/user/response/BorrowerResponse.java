package com.emiLoan.EMILoan.dto.user.response;

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
public class BorrowerResponse {
    private UserResponse user;
    private UUID borrowerId;
    private String borrowerCode;
    private BigDecimal monthlyIncome;
    private Integer existingLoanCount;
}
