package com.emiLoan.EMILoan.strategy.payment;


import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.strategy.payment.PaymentGatewayStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class CardPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentStatus processPayment(BigDecimal amount) {
        log.info("Simulating CARD Payment of ₹{} via Secure Card Gateway...", amount);
         return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentMode getSupportedMode() {
        return PaymentMode.CARD;
    }
}
