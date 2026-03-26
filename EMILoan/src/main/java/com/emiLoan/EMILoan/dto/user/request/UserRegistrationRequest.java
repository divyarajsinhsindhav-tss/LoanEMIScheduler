package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @Pattern(regexp = "^\\d{10}$", message = "Phone must be 10 digits")
    private String phone;

    @PositiveOrZero(message = "Monthly income cannot be negative")
    private BigDecimal monthlyIncome;
}