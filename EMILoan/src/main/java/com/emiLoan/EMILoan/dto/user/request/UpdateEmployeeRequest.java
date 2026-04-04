package com.emiLoan.EMILoan.dto.user.request;

//employee means loan officer
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateEmployeeRequest {

    @DecimalMin(value = "0.0", message = "Salary cannot be negative")
    private BigDecimal salary;

    private Boolean isActive;
}