package com.struchev.invest.service.candle;

import com.struchev.invest.service.processor.PurchaseService;
import com.struchev.invest.service.tinkoff.ITinkoffCommonAPI;
import com.struchev.invest.strategy.StrategySelector;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import java.util.ArrayList;

/**
 * Service to observe candles
 */
@Service
@RequiredArgsConstructor
@DependsOn({"candleHistoryService"})
@ConditionalOnProperty(name = "candle.listener.enabled", havingValue = "true")
@Slf4j
public class CandleListenerService {
    private final CandleHistoryService candleHistoryService;
    private final PurchaseService purchaseService;
    private final StrategySelector strategySelector;
    private final ITinkoffCommonAPI tinkoffCommonAPI;

    private void startToListen() {
        var figies = strategySelector.getFigiesForActiveStrategies();
        if (CollectionUtils.isEmpty(figies)) {
            log.warn("No figies to subscribe");
            return;
        }

        log.info("Start to listen candle events..");
        try {
            tinkoffCommonAPI.getApi().getMarketDataStreamService()
                    .newStream("candles_stream", item -> {
                        log.trace("New data in streaming api: {}", item);
                        if (item.hasCandle()) {
                            var candle = HistoricCandle.newBuilder();
                            candle.setClose(item.getCandle().getClose());
                            candle.setOpen(item.getCandle().getOpen());
                            candle.setHigh(item.getCandle().getHigh());
                            candle.setLow(item.getCandle().getLow());
                            candle.setTime(item.getCandle().getTime());
                            var candleDomainEntity = candleHistoryService.addOrReplaceCandles(candle.build(), item.getCandle().getFigi());
                            purchaseService.observeCandleNoThrow(candleDomainEntity);
                        }
                    }, e -> {
                        log.error("An error in candles_stream, listener will be restarted", e);
                        startToListen();
                    })
                    .subscribeCandles(new ArrayList<>(figies));
        } catch (Throwable th) {
            log.error("An error in subscriber, listener will be restarted", th);
            startToListen();
            throw th;
        }
    }

    @PostConstruct
    void init() {
        new Thread(this::startToListen, "event-listener").start();
    }
}
