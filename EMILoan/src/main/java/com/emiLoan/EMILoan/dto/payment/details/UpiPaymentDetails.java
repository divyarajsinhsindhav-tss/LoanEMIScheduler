package com.emiLoan.EMILoan.dto.payment.details;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpiPaymentDetails extends PaymentMethodDetails {

    @NotBlank(message = "UPI ID is required")
    private String upiId;
}