package com.struchev.invest.service.processor;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.strategy.AStrategy;

import java.math.BigDecimal;

interface ICalculatorService<T extends AStrategy> {
    boolean isShouldBuy(T strategy, CandleDomainEntity candle);

    boolean isShouldSell(T strategy, CandleDomainEntity candle, BigDecimal purchaseRate);

    AStrategy.Type getStrategyType();
}
