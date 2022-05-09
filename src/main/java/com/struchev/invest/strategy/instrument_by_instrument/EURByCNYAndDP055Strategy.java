package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByCNYAndDP055Strategy extends AInstrumentByInstrumentStrategy {

    private Map FIGIES = Map.of(
            "BBG0013HRTL0", 7000, // CNY
            "BBG0013HJJ31", 1000 // EUR
    );

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public float getMinimalDropPercent() {
        return 0.55f;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

