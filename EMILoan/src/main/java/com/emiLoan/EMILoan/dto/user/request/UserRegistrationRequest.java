package com.emiLoan.EMILoan.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be under 50 characters")
    private String firstName;

    @Size(max = 50, message = "Last name must be under 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be under 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Phone must be exactly 10 digits")
    private String phone;

    @NotBlank(message = "PAN is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN format (e.g., ABCDE1234F)")
    private String pan;
}