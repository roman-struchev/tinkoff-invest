package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CRH6BySiH6ByEuH6Dp03Strategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("CRH6", 7) // CNY
            .put("SiH6", 1) // USD
            .put("EuH6", 1) // EUR
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.3f;
    }
}