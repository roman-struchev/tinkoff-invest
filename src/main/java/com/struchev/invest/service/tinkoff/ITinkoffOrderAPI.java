package com.struchev.invest.service.tinkoff;

import com.struchev.invest.service.dictionary.InstrumentService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

public interface ITinkoffOrderAPI {
    @AllArgsConstructor
    @Data
    @Builder
    class OrderResult {
        BigDecimal commission;
        BigDecimal price;
    }

    OrderResult buy(InstrumentService.Instrument instrument, BigDecimal price, Integer count);

    OrderResult sell(InstrumentService.Instrument instrument, BigDecimal price, Integer count);
}
