package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

@Component
public class BuyP10AndTPP60AndSL10PercentStrategy extends AInstrumentByFiatTestStrategy {

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder().lessThenPercentile(10).build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder().takeProfitPercentile(60).stopLossPercent(10f).build();
    }
}
