package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class USDByCNYStrategy extends AInstrumentByInstrumentStrategy {

    private Map<String, Integer> FIGI = new ImmutableMap.Builder<String, Integer>()
            .put("BBG0013HRTL0", 6000) // CNY
            .put("BBG0013HGFT4", 1000) // USD
            .build();

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
