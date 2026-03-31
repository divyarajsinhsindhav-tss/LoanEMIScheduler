package com.emiLoan.EMILoan.strategy.payment;


import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.dto.payment.details.PaymentMethodDetails;

import java.math.BigDecimal;

public interface PaymentGatewayStrategy {
    PaymentMode getSupportedMode();
    PaymentStatus processPayment(BigDecimal amount, PaymentMethodDetails details);
}