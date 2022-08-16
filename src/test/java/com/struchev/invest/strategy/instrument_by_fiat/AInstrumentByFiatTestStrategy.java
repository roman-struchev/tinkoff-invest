package com.struchev.invest.strategy.instrument_by_fiat;

import java.util.Map;

public abstract class AInstrumentByFiatTestStrategy extends AInstrumentByFiatStrategy {

    private static final Map FIGIES = Map.of(
//            "BBG004730N88", 10,    // SBER
            "BBG006L8G4H1", 1,    //Yandex
//            "BBG004S683W7", 10, //Аэрофлот
//            "BBG00178PGX3", 1,    //VK
//            "BBG004730JJ5", 10,  // Московская биржа
//            "BBG004S681W1", 10,  // МТС
//            "BBG000BBXB74", 1,  // BMW
//            "BBG000BFCS17", 1,  // Volkswagen
//            "BBG000BLMTR3", 1,  // Puma
              "BBG000B9XRY4", 1  // Apple
//            "BBG000BVPV84", 1,  // Amazon
//            "BBG000BBQCY0", 1,  // AMD
//            "BBG000N9MNX3", 1,  // Tesla
//            "BBG000C5HS04", 1  // NIKE
//            "BBG111111111", 1,  // Тинкофф NASDAQ
//            "TCS00A102EM7", 1,  // Тинкофф индекс IPO
//            "BBG00ZGF7771", 1,  // Coinbase
//            "BBG00PNN7C40", 1,  // Dynatrace
//            "BBG000BG1SX2", 1,  // Caca cola
//            "BBG008NMBXN8", 1   // Robinhood
//            "BBG00ZGF7771", 1,  // Coinbase
//            "BBG0013HJJ31", 1, // EUR
//            "BBG0013HGFT4", 1, // USD
//            "BBG0013HRTL0", 1 // CNY
    );

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
