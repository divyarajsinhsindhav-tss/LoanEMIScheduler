package com.emiLoan.EMILoan.strategy.payment;


import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import java.math.BigDecimal;

public interface PaymentGatewayStrategy {
    PaymentStatus processPayment(BigDecimal amount);
    PaymentMode getSupportedMode();
}