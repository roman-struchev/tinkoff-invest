package com.struchev.invest.strategy.instrument_by_instrument;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CNYFByUSDFByEURFByGoldFStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
            .put("FUTCNYRUBF00", 7) // CNY
            .put("FUTUSDRUBF00", 1) // USDRUBF
            .put("FUTEURRUBF00", 1) // EURRUBF
            .put("FUTGLDRUBF00", 7) // GLDRUBF
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}