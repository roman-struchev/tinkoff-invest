package com.struchev.invest.strategy.instrument_by_fiat;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class AInstrumentByFiatTestStrategy extends AInstrumentByFiatStrategy {

    private static final Map<String, Integer> FIGIES = new ImmutableMap.Builder<String, Integer>()
//            .put("BBG004730N88", 1)  // SBER
//            .put("BBG006L8G4H1", 1)  // Yandex
//            .put("TCS00A107UL4", 1)  // Тинькофф
//            .put("TCS00A106YF0", 1)  // VK
//            .put("BBG004S681W1", 1)  // МТС
//            .put("BBG004731354", 1)  // Роснефть
//            .put("BBG004730JJ5", 1)  // Московская биржа
//            .put("BBG000VHQTD1", 1)  // Серебро
//            .put("BBG000VJ5YR4", 1)  // Золото
//            .put("BBG000BBXB74", 1)  // BMW
//            .put("BBG000BFCS17", 1)  // Volkswagen
//            .put("BBG000BLMTR3", 1)  // Puma
//            .put("BBG000B9XRY4", 1)  // Apple
//            .put("BBG000BVPV84", 1)  // Amazon
//            .put("BBG000BBQCY0", 1)  // AMD
//            .put("BBG000N9MNX3", 1)  // Tesla
//            .put("BBG000C5HS04", 1)  // NIKE
//            .put("BBG111111111", 1)  // Тинкофф NASDAQ
//            .put("TCS00A102EM7", 1)  // Тинкофф индекс IPO
//            .put("BBG00ZGF7771", 1)  // Coinbase
//            .put("BBG00PNN7C40", 1)  // Dynatrace
//            .put("BBG000BG1SX2", 1)  // Coca cola
//            .put("BBG008NMBXN8", 1)  // Robinhood
//            .put("BBG0013HJJ31", 1)  // EUR
//            .put("BBG0013HGFT4", 1)  // USD
//            .put("BBG0013HRTL0", 1)   // CNY
            .put("BBG00Y91R9T3", 1)   // OZON
            .put("RU000A106T36", 1)   // Astra
            .put("TCS00A105EX7", 1)   // Whoosh
            .build();

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
