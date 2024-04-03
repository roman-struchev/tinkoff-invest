package com.struchev.invest.service.processor;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.strategy.AStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис - фасад для калькуляторов по разным стратегиям
 */
@Service
@RequiredArgsConstructor
public class CalculatorFacade {
    private final List<ICalculatorService> calculateServices;
    private Map<AStrategy.Type, ICalculatorService> calculateServiceByType;

    public <T extends AStrategy> boolean isShouldBuy(T strategy, CandleDomainEntity candle) {
        return calculateServiceByType.get(strategy.getType()).isShouldBuy(strategy, candle);
    }

    public <T extends AStrategy> boolean isShouldSell(T strategy, CandleDomainEntity candle, BigDecimal purchasePrice) {
        return calculateServiceByType.get(strategy.getType()).isShouldSell(strategy, candle, purchasePrice);
    }

    @PostConstruct
    private void init() {
        calculateServiceByType = calculateServices.stream().collect(Collectors.toMap(ICalculatorService::getStrategyType, c -> c));
    }
}
