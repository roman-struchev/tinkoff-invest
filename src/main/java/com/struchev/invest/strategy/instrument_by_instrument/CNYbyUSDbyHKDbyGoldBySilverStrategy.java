package com.struchev.invest.strategy.instrument_by_instrument;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CNYbyUSDbyHKDbyGoldBySilverStrategy extends AInstrumentByInstrumentStrategy {

    private final Map<String, Integer> FIGI = Map.of(
            "BBG0013HRTL0", 6000, // CNY
            "BBG0013HGFT4", 1000, // USD
            "BBG0013HSW87", 7000, // HKD
            "BBG000VJ5YR4", 14,   // золото
            "BBG000VHQTD1", 1000   // серебро
    );

    public Map<String, Integer> getFigies() {
        return FIGI;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}