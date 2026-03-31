package com.emiLoan.EMILoan.strategy.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.dto.payment.details.NetBankingPaymentDetails;
import com.emiLoan.EMILoan.dto.payment.details.PaymentMethodDetails;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class NetBankingPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentStatus processPayment(BigDecimal amount, PaymentMethodDetails details) {

        if (!(details instanceof NetBankingPaymentDetails netBankingDetails)) {
            log.error("Gateway Mismatch: Expected NetBankingPaymentDetails but received {}", details.getClass().getSimpleName());
            throw new BusinessRuleException("Invalid payment details provided for Net Banking transaction.");
        }

        String bankCode = netBankingDetails.getBankCode();

        log.info("Initiating Net Banking Redirect for Bank Code: {}", bankCode);
        log.info("Amount: ₹{}", amount);
        log.info("Simulating Bank Aggregator API Response... SUCCESS.");

        return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentMode getSupportedMode() {
        return PaymentMode.NET_BANKING;
    }
}