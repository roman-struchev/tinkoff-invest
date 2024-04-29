package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CNYbyUSDbyHKDbyKZTByBYNStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HRTL0", 7000) // CNY
            .put("BBG0013HGFT4", 1000) // USD
            .put("BBG0013HSW87", 8000) // HKD
            .put("BBG0013HG026", 400000) // KZT
            .put("BBG00D87WQY7", 3000) // BYN
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}