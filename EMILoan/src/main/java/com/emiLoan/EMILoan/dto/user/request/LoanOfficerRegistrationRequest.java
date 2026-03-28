package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class LoanOfficerRegistrationRequest extends UserRegistrationRequest {

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @NotNull(message = "Salary is required")
    @PositiveOrZero(message = "Salary cannot be negative")
    private BigDecimal salary;
}