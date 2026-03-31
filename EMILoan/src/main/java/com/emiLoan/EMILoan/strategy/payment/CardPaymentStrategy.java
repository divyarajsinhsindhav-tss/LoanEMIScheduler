package com.emiLoan.EMILoan.strategy.payment;

import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.common.enums.PaymentStatus;
import com.emiLoan.EMILoan.dto.payment.details.CardPaymentDetails;
import com.emiLoan.EMILoan.dto.payment.details.PaymentMethodDetails;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class CardPaymentStrategy implements PaymentGatewayStrategy {

    @Override
    public PaymentStatus processPayment(BigDecimal amount, PaymentMethodDetails details) {

        if (!(details instanceof CardPaymentDetails cardDetails)) {
            log.error("Gateway Mismatch: Expected CardPaymentDetails but received {}", details.getClass().getSimpleName());
            throw new BusinessRuleException("Invalid payment details provided for Credit/Debit Card transaction.");
        }

        String cardNumber = cardDetails.getCardNumber();
        String maskedCard = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);

        log.info("Initiating Payment Gateway Tokenization for Card...");
        log.info("Amount: ₹{}", amount);
        log.info("Cardholder: {}", cardDetails.getCardholderName());
        log.info("Card Number: {}", maskedCard);
        log.info("Simulating Visa/Mastercard Network Response... SUCCESS.");

        return PaymentStatus.SUCCESS;
    }

    @Override
    public PaymentMode getSupportedMode() {
        return PaymentMode.CARD;
    }
}