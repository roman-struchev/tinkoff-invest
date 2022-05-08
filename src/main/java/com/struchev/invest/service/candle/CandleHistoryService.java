package com.struchev.invest.service.candle;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.repository.CandleRepository;
import com.struchev.invest.service.tinkoff.ITinkoffCommonAPI;
import com.struchev.invest.strategy.StrategySelector;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
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

    @Value("${candle.history.duration}")
    private Duration historyDuration;

    @Transactional
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
                    log.trace("Replaced candle {} to candle {}:", candleDomainEntity, newCandle);
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
            log.trace("Add new candle {}", candleDomainEntity);
            return candleDomainEntity;
        } catch (Exception e) {
            log.error("Can't add candle", e);
            throw e;
        }
    }

    public List<CandleDomainEntity> getCandlesByFigiBetweenDateTimes(String figi, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        var candles = candleRepository.findByFigiAndIntervalAndBetweenDateTimes(figi,
                "1min", startDateTime, endDateTime);
        return candles;
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
            var candles = tinkoffCommonAPI.getApi().getMarketDataService().getCandles(figi,
                    start.toInstant(), end.toInstant(), resolution).get();
            return candles;
        } catch (Exception e) {
            log.error("Can't get candles for figi {}", e);
            Thread.sleep(10000);
            return requestCandles(figi, start, end, resolution, tries--);
        }
    }

    /**
     * Request candle history by API and save it to DB
     *
     * @param days
     * @return
     */
    public void requestCandlesHistoryForDays(long days) {
        if (days == 0) {
            return;
        }

        var now = OffsetDateTime.now();
        var counter = new AtomicLong();
        var figies = strategySelector.getFigiesForActiveStrategies();
        log.info("Start to load history {} days, for figies {}", days, figies);
        figies.stream().forEach(figi -> {
            LongStream.range(0, days).forEach(i -> {
                log.info("Request candles {}, {} day", figi, i);
                var candles = requestCandles(figi, now.minusDays(i + 1), now.minusDays(i), CandleInterval.CANDLE_INTERVAL_1_MIN, 12);
                candles.forEach(c -> addOrReplaceCandles(c, figi));
            });
            counter.addAndGet(1);
        });

        var count = counter.get();
        log.info("Loaded to load history for {} days", days);
    }

    @PostConstruct
    void init() {
        requestCandlesHistoryForDays(historyDuration.toDays());
    }
}
