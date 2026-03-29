package com.emiLoan.EMILoan.strategy.payment;


import com.emiLoan.EMILoan.common.enums.PaymentMode;
import com.emiLoan.EMILoan.exceptions.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentStrategyFactory {
    private final Map<PaymentMode, PaymentGatewayStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentGatewayStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentGatewayStrategy::getSupportedMode, s -> s));
    }

    public PaymentGatewayStrategy getStrategy(PaymentMode mode) {
        PaymentGatewayStrategy strategy = strategies.get(mode);
        if (strategy == null) {
            throw new BusinessRuleException("Unsupported Payment Mode: " + mode);
        }
        return strategy;
    }
}