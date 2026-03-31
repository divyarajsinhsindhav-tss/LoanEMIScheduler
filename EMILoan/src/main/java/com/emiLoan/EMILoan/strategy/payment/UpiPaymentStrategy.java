package com.emiLoan.EMILoan.strategy.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.dto.payment.details.PaymentMethodDetails;
import com.emiLoan.EMILoan.dto.payment.details.UpiPaymentDetails;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class UpiPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentStatus processPayment(BigDecimal amount, PaymentMethodDetails details) {

        if (!(details instanceof UpiPaymentDetails upiDetails)) {
            log.error("Gateway Mismatch: Expected UpiPaymentDetails but received {}", details.getClass().getSimpleName());
            throw new BusinessRuleException("Invalid payment details provided for UPI transaction.");
        }

        String upiId = upiDetails.getUpiId();

        log.info("Initiating UPI Request...");
        log.info("Amount: ₹{}", amount);
        log.info("Target VPA (UPI ID): {}", upiId);
        log.info("Simulating Bank Response... SUCCESS.");

        return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentMode getSupportedMode() {
        return PaymentMode.UPI;
    }
}