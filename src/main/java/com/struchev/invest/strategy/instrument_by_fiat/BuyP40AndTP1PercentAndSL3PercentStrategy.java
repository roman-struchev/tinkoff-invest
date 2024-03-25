package com.struchev.invest.strategy.instrument_by_fiat;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BuyP40AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private static final Map FIGIES = Map.of(
            "BBG000VJ5YR4", 8,   // золото
            "BBG000VHQTD1", 6000,    // серебро
            "BBG0013HRTL0", 4,       // юань
            "BBG004S681W1", 200,     // мтс
            "BBG004730JJ5", 250,     // московская биржа
            "BBG006L8G4H1", 15       // яндекс
    );

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
