package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BYNbyKZTStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG00D87WQY7", 30000) // BYN
            .put("BBG0013HG026", 400000) // KZT
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}