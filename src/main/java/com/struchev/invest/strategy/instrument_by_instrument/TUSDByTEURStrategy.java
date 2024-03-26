package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TUSDByTEURStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("BBG000000000", 100) // TUSD
            .put("BBG000000002", 100) // TEUR
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
