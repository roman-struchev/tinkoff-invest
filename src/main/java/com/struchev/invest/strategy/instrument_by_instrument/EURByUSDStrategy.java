package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EURByUSDStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGI = Map.of(
            "BBG0013HJJ31", 1000,// EUR
            "BBG0013HGFT4", 1000 // USD
    );

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
