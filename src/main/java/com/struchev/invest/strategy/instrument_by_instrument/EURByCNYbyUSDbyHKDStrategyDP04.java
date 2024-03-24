package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByCNYbyUSDbyHKDStrategyDP04 extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGI = Map.of(
            "BBG0013HRTL0", 6000, // CNY
            "BBG0013HJJ31", 1000, // EUR
            "BBG0013HGFT4", 1000, // USD
            "BBG0013HSW87", 7000 // HKD
    );

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.4f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}