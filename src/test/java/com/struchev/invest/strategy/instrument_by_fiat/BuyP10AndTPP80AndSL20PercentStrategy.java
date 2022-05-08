package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

@Component
public class BuyP10AndTPP80AndSL20PercentStrategy extends AInstrumentByFiatTestStrategy {

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder().lessThenPercentile(10).build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder().takeProfitPercentile(80).stopLossPercent(20f).build();
    }
}
