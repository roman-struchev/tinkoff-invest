package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CNYbyUSDbyHKDbyKZTbyGoldBySilverStrategyDP045 extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HRTL0", 7000) // CNY
            .put("BBG0013HGFT4", 1000) // USD
            .put("BBG0013HSW87", 8000) // HKD
            .put("BBG0013HG026", 400000) // KZT
            .put("BBG000VJ5YR4", 14)   // золото
            .put("BBG000VHQTD1", 1000)   // серебро
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
    public boolean isOnlySell() {
        return true;
    }
}