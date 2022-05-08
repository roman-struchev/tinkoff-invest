package com.struchev.invest.strategy.instrument_by_fiat;

import com.struchev.invest.strategy.AStrategy;
import org.springframework.stereotype.Component;

@Component
public class BuyP5AndTPP60AndSL10PercentStrategy extends AInstrumentByFiatTestStrategy {

    @Override
    public BuyCriteria getBuyCriteria() {
        return BuyCriteria.builder().lessThenPercentile(10).build();
    }

    @Override
    public SellCriteria getSellCriteria() {
        return SellCriteria.builder().takeProfitPercentile(60).stopLossPercent(10f).build();
    }
}
