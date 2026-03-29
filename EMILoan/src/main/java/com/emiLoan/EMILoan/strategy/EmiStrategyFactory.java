package com.emiLoan.EMILoan.strategy;


import com.emiLoan.EMILoan.exceptions.StrategyNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class EmiStrategyFactory {

    private final Map<String, EmiCalculationStrategy> strategyMap;

    @Autowired
    public EmiStrategyFactory(List<EmiCalculationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        EmiCalculationStrategy::getStrategyName,
                        strategy -> strategy
                ));
    }

    public EmiCalculationStrategy getStrategy(String strategyName) {
        if (strategyName == null || strategyName.trim().isEmpty()) {
            throw new StrategyNotFoundException("Strategy name cannot be null or empty.");
        }

        String normalizedName = strategyName.trim().toUpperCase();

        if ("REJECTED".equals(normalizedName)) {
            throw new StrategyNotFoundException("Cannot generate an EMI schedule for a REJECTED loan.");
        }

        EmiCalculationStrategy strategy = strategyMap.get(normalizedName);

        if (strategy == null) {
            throw new StrategyNotFoundException("No calculation strategy found for: " + normalizedName);
        }

        return strategy;
    }
}