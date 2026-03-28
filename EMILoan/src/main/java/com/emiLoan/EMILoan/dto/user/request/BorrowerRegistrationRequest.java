package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class BorrowerRegistrationRequest extends UserRegistrationRequest {

    @NotNull(message = "Monthly income is required")
    @PositiveOrZero(message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;
}