package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

@Component
public class CNYbyUSDbyHKDStrategyDP045 extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HRTL0", 7000) // CNY
            .put("BBG0013HGFT4", 1000) // USD
            .put("BBG0013HSW87", 8000) // HKD
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.45f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Duration getForceToSellDuration() {
        return Duration.ofDays(5);
    }
}