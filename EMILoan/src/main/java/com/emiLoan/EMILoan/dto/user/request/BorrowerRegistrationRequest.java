package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
public class BorrowerRegistrationRequest extends UserRegistrationRequest {
    @PositiveOrZero(message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;
}
