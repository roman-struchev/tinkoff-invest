package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class JPYByCNYStrategy extends AInstrumentByInstrumentStrategy {

    private  final Map<String, Integer> FIGI = Map.of(
            "BBG0013HQ524", 1000, // JPY
            "BBG0013HRTL0", 6000 // CNY
    );

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

