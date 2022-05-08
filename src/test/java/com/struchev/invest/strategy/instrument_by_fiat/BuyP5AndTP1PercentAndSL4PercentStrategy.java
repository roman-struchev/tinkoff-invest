package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

@Component
public class BuyP5AndTP1PercentAndSL4PercentStrategy extends AInstrumentByFiatTestStrategy {

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder().lessThenPercentile(5).build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder().takeProfitPercent(1f).stopLossPercent(4f).build();
    }
}
