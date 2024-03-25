package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuyP10AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private static final Map FIGIES = Map.of(
            "TCS00A106YF0", 80, // ВК
            "BBG004730N88", 200 // сбер
    );

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public AInstrumentByFiatStrategy.BuyCriteria getBuyCriteria() {
        return AInstrumentByFiatStrategy.BuyCriteria.builder().lessThenPercentile(10).build();
    }

    @Override
    public AInstrumentByFiatStrategy.SellCriteria getSellCriteria() {
        return AInstrumentByFiatStrategy.SellCriteria.builder().takeProfitPercent(1f).stopLossPercent(3f).build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
