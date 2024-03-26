package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByCNYbyUSDbyHKDStrategyDP025 extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HRTL0", 6000) // CNY
            .put("BBG0013HJJ31", 1000) // EUR
            .put("BBG0013HGFT4", 1000) // USD
            .put("BBG0013HSW87", 7000) // HKD
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.25f;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}