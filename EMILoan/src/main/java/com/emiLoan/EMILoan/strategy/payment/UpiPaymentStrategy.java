package com.emiLoan.EMILoan.strategy.payment;


import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Slf4j
@Component
public class UpiPaymentStrategy implements PaymentGatewayStrategy {
    @Override
    public PaymentStatus processPayment(BigDecimal amount) {
        log.info("Simulating UPI Payment of ₹{} via Virtual UPI Gateway...", amount);
        return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentMode getSupportedMode() {
        return PaymentMode.UPI;
    }
}