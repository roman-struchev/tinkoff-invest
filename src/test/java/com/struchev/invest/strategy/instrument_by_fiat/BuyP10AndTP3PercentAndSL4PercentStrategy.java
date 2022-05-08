package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

@Component
public class BuyP10AndTP3PercentAndSL4PercentStrategy extends AInstrumentByFiatTestStrategy {

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder().lessThenPercentile(10).build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder().takeProfitPercent(3f).stopLossPercent(4f).build();
    }
}
