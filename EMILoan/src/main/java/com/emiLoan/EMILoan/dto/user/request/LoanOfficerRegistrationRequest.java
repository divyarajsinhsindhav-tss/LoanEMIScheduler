package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
public class LoanOfficerRegistrationRequest extends UserRegistrationRequest{

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    @PositiveOrZero(message = "Salary cannot be negative")
    private BigDecimal salary;
}
