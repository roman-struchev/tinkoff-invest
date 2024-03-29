package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByUSDStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HJJ31", 1000)// EUR
            .put("BBG0013HGFT4", 1000) // USD
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
