package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByCNYbyUSDStrategyDP025 extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGI = Map.of(
            "BBG0013HRTL0", 6000, // CNY
            "BBG0013HJJ31", 1000, // EUR
            "BBG0013HGFT4", 1000 // USD
    );

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.25f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}