package com.struchev.invest.service.candle;

import com.struchev.invest.service.notification.NotificationService;
import com.struchev.invest.service.processor.PurchaseService;
import com.struchev.invest.service.tinkoff.ITinkoffCommonAPI;
import com.struchev.invest.strategy.StrategySelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;

import javax.annotation.PostConstruct;
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
    private final NotificationService notificationService;

    private void startToListen() {
        var figies = strategySelector.getFigiesForActiveStrategies();
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
                        purchaseService.observeNewCandleNoThrow(candleDomainEntity);
                    }
                }, e -> log.error("candles_stream error", e))
                .subscribeCandles(new ArrayList<>(figies));
    }

    private void startToListenInThread(int number) {
        new Thread(() -> {
            try {
                notificationService.sendMessageAndLog("Listening candle events..");
                startToListen();
            } catch (Throwable th) {
                log.error("An error, listener will be restarted", th);
                startToListenInThread(number + 1);
                throw th;
            }
        }, "event-listener-" + number).start();
    }

    @PostConstruct
    void init() {
        startToListenInThread(1);
    }
}
