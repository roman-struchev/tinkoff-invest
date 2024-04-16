package com.struchev.invest.service.candle;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.repository.CandleRepository;
import com.struchev.invest.service.tinkoff.ITinkoffCommonAPI;
import com.struchev.invest.strategy.AStrategy;
import com.struchev.invest.strategy.StrategySelector;
import com.struchev.invest.strategy.instrument_by_fiat.AInstrumentByFiatStrategy;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Service to load and store actual history of candles
 */
@Service
@Slf4j
@DependsOn({"instrumentService"})
@RequiredArgsConstructor
public class CandleHistoryService {
    private final ITinkoffCommonAPI tinkoffCommonAPI;
    private final StrategySelector strategySelector;
    private final CandleRepository candleRepository;
    private final EntityManager entityManager;

    private Map<String, OffsetDateTime> firstCandleDateTimeByFigi;
    private Map<String, List<CandleDomainEntity>> candlesLocalCache;

    @Value("${candle.history.duration}")
    private Duration historyDuration;
    @Value("${candle.listener.enabled:false}")
    private Boolean isCandleListenerEnabled;

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public CandleDomainEntity addOrReplaceCandles(HistoricCandle newCandle, String figi) {
        try {
            var dateTime = ConvertorUtils.toOffsetDateTime(newCandle.getTime().getSeconds());
            var closingProse = ConvertorUtils.toBigDecimal(newCandle.getClose(), null);
            var openPrice = ConvertorUtils.toBigDecimal(newCandle.getOpen(), null);
            var lowestPrice = ConvertorUtils.toBigDecimal(newCandle.getLow(), null);
            var highestPrice = ConvertorUtils.toBigDecimal(newCandle.getHigh(), null);

            log.trace("Candle for {} by {} with openPrice {}, closingProse {}", figi, dateTime, openPrice, closingProse);
            var candleDomainEntity = candleRepository.findByFigiAndIntervalAndDateTime(figi, "1min", dateTime);
            if (candleDomainEntity != null) {
                if (closingProse.compareTo(candleDomainEntity.getClosingPrice()) != 0
                        || highestPrice.compareTo(candleDomainEntity.getHighestPrice()) != 0
                        || openPrice.compareTo(candleDomainEntity.getOpenPrice()) != 0
                        || lowestPrice.compareTo(candleDomainEntity.getLowestPrice()) != 0) {
                    log.trace("Replaced existing candle {} to candle {}:", candleDomainEntity, newCandle);
                    candleDomainEntity.setClosingPrice(closingProse);
                    candleDomainEntity.setHighestPrice(highestPrice);
                    candleDomainEntity.setLowestPrice(lowestPrice);
                    candleDomainEntity.setOpenPrice(openPrice);
                    candleDomainEntity = candleRepository.save(candleDomainEntity);
                }
                return candleDomainEntity;
            }
            candleDomainEntity = CandleDomainEntity.builder()
                    .figi(figi)
                    .closingPrice(closingProse)
                    .highestPrice(highestPrice)
                    .lowestPrice(lowestPrice)
                    .openPrice(openPrice)
                    .interval("1min")
                    .dateTime(dateTime)
                    .build();
            candleDomainEntity = candleRepository.save(candleDomainEntity);
            log.trace("Saved new candle {}", candleDomainEntity);
            return candleDomainEntity;
        } catch (Exception e) {
            log.error("Can't save new candle", e);
            throw e;
        }
    }

    public List<CandleDomainEntity> getCandlesByFigiBetweenDateTimes(String figi, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        // если слушатель выключен - значит запускаем не в продакшн режиме, можем хранить свечки в памяти, а не на диске (БД)
        if (!isCandleListenerEnabled) {
            var candlesByFigi = candlesLocalCache.get(figi);
            if (candlesByFigi.isEmpty()) {
                throw new UnsupportedOperationException("Candles not found in local cache for " + figi);
            }
            return candlesByFigi.stream()
                    .filter(c -> c.getDateTime().isAfter(startDateTime))
                    .filter(c -> c.getDateTime().isBefore(endDateTime) || c.getDateTime().isEqual(endDateTime))
                    .collect(Collectors.toList());
        }
        return candleRepository.findByFigiAndIntervalAndBetweenDateTimes(figi, "1min", startDateTime, endDateTime);
    }

    /**
     * Request candles by api, try several times with 10s pause in case of error
     *
     * @param figi
     * @param start
     * @param end
     * @param resolution
     * @param tries
     * @return
     */
    @SneakyThrows
    private List<HistoricCandle> requestCandles(String figi, OffsetDateTime start, OffsetDateTime end, CandleInterval resolution, int tries) {
        try {
            return tinkoffCommonAPI.getApi().getMarketDataService().getCandles(figi, start.toInstant(),
                    end.toInstant(), resolution).get();
        } catch (Exception e) {
            log.error("Can't get candles for figi {}", figi, e);
            Thread.sleep(10000);
            return requestCandles(figi, start, end, resolution, tries - 1);
        }
    }

    /**
     * Request candle history by API and save it to DB
     *
     * @param days
     */
    @SneakyThrows
    public void requestCandlesHistoryForDays(long days) {
        if (days == 0) {
            return;
        }

        var now = OffsetDateTime.now();
        var figies = strategySelector.getFigiesForActiveStrategies();
        log.info("Start to load history {} days, for figies {}", days, figies);
        figies.forEach(figi ->
                LongStream.range(0, days).forEach(i -> {
                    log.info("Request candles {}, {} day", figi, i);
                    var candles = requestCandles(figi, now.minusDays(i + 1), now.minusDays(i), CandleInterval.CANDLE_INTERVAL_1_MIN, 12);
                    log.info("Save candles to DB {}, {} day", figi, i);
                    candles.forEach(c -> addOrReplaceCandles(c, figi));
                    log.info("Candles was saved to DB {}, {} day", figi, i);
                })
        );
        log.info("Loaded to load history for {} days", days);
    }

    public OffsetDateTime getFirstCandleDateTime(String figi) {
        return firstCandleDateTimeByFigi.computeIfAbsent(figi, f -> {
            var firstCandle = candleRepository.findByFigiAndIntervalOrderByDateTime(f, "1min").getFirst();
            return firstCandle.getDateTime();
        });
    }

    @PostConstruct
    void init() {
        firstCandleDateTimeByFigi = new ConcurrentHashMap<>();
        var historyDurationByStrategies = strategySelector.getActiveStrategies().stream()
                .filter(s -> s.getType() == AStrategy.Type.instrumentByFiat)
                .map(s -> ((AInstrumentByFiatStrategy) s).getHistoryDuration())
                .reduce((d1, d2) -> d1.toSeconds() > d2.toSeconds() ? d1 : d2)
                .stream().findFirst().orElse(Duration.ZERO);
        requestCandlesHistoryForDays(historyDurationByStrategies.toSeconds() > historyDuration.toSeconds()
                ? historyDurationByStrategies.toDays() : historyDuration.toDays());

        if (!isCandleListenerEnabled) {
            var candles = candleRepository.findByIntervalOrderByDateTime("1min");
            candlesLocalCache = candles.stream()
                    .peek(entityManager::detach)
                    .collect(Collectors.groupingBy(CandleDomainEntity::getFigi));
        }
    }
}
