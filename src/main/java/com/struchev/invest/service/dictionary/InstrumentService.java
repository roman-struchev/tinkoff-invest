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
        // load all instruments async
        var sharesFuture = tinkoffCommonAPI.getApi().getInstrumentsService().getAllShares();
        var futuresFuture = tinkoffCommonAPI.getApi().getInstrumentsService().getAllFutures();
        var boundsFuture = tinkoffCommonAPI.getApi().getInstrumentsService().getAllBonds();
        var etfsFuture = tinkoffCommonAPI.getApi().getInstrumentsService().getAllEtfs();
        var currenciesFuture = tinkoffCommonAPI.getApi().getInstrumentsService().getAllCurrencies();

        // save instruments to memory
        instrumentByFigi = new ConcurrentHashMap<>();
        sharesFuture.get().forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
        futuresFuture.get().forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
        boundsFuture.get().forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
        etfsFuture.get().forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
        currenciesFuture.get().forEach(i -> instrumentByFigi.put(i.getFigi(), new Instrument(i.getFigi(), i.getCurrency(), i.getName(), i.getLot())));
    }
}
