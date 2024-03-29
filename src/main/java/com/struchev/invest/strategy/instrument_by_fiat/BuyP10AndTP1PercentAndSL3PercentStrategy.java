package com.struchev.invest.strategy.instrument_by_fiat;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuyP10AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private static final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG004S683W7", 10) // Аэрофлот
            .put("BBG008NMBXN8", 1)  // Robinhood
            .build();

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
        return false;
    }
}
