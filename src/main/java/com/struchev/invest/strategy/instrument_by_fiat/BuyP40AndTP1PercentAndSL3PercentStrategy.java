package com.struchev.invest.strategy.instrument_by_fiat;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuyP40AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private static final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG000VJ5YR4", 8)       // золото
            .put("BBG000VHQTD1", 600)     // серебро
            .put("BBG0013HRTL0", 4000)    // юань
            .put("BBG004S681W1", 200)     // мтс
            .put("BBG004730JJ5", 250)     // московская биржа
            .put("BBG006L8G4H1", 15)      // яндекс
            .put("BBG004731354", 100)     // роснефть
            .put("TCS00A105EX7", 150)     // whoosh
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public AInstrumentByFiatStrategy.BuyCriteria getBuyCriteria() {
        return AInstrumentByFiatStrategy.BuyCriteria.builder().lessThenPercentile(40).build();
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
