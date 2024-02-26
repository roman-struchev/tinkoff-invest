package com.struchev.invest.service.dictionary;

import com.struchev.invest.service.tinkoff.ITinkoffCommonAPI;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to provide details for any trade instrument
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentService {
    private final ITinkoffCommonAPI tinkoffCommonAPI;
    private Map<String, Instrument> instrumentByFigi;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Instrument {
        String figi;
        String currency;
        String name;
        int lot;
    }

    public Instrument getInstrument(String figi) {
        var instrument = instrumentByFigi.get(figi);
        if (instrument == null) {
            log.warn("Instrument not found: {}", figi);
        }
        return instrument;
    }

    @SneakyThrows
    @PostConstruct
    @Retryable
    private void init() {
        // load all instruments to memory
        instrumentByFigi = new ConcurrentHashMap<>();

        var shares = tinkoffCommonAPI.getApi().getInstrumentsService().getAllSharesSync();
        shares.forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));

        var futures = tinkoffCommonAPI.getApi().getInstrumentsService().getAllFuturesSync();
        futures.forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));

        var bounds = tinkoffCommonAPI.getApi().getInstrumentsService().getAllBondsSync();
        bounds.forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));

        var etfs = tinkoffCommonAPI.getApi().getInstrumentsService().getAllEtfsSync();
        etfs.forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));

        var currencies = tinkoffCommonAPI.getApi().getInstrumentsService().getAllCurrenciesSync();
        currencies.forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
    }
}
