package com.struchev.invest.service.processor;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.entity.OrderDomainEntity;
import com.struchev.invest.service.notification.NotificationService;
import com.struchev.invest.service.order.OrderService;
import com.struchev.invest.service.price.PricesService;
import com.struchev.invest.strategy.AStrategy;
import com.struchev.invest.strategy.StrategySelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

/**
 * Сервис обрабатывает поток свечей
 * Принимает решение с помощью калькулятора о покупке/продаже инструмента в рамках включенных стратегий
 * Отправляет ордеры (покупка, продажа) на исполнение
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PurchaseService {
    private final OrderService orderService;
    private final StrategySelector strategySelector;
    private final NotificationService notificationService;
    private final CalculatorFacade calculator;
    private final PricesService pricesService;

    /**
     * Обработчик новой свечи по всем стратегиям
     * Запускает параллельно обработку свечи по каждой стратегии
     * Не выбрасывает исключение, логирует ошибку
     *
     * @param candle
     */
    public synchronized void observeCandleNoThrow(CandleDomainEntity candle) {
        try {
            log.debug("Observe candle event: {}", candle);
            pricesService.updatePrice(candle.getFigi(), candle.getClosingPrice());
            var strategies = strategySelector.suitableByFigi(candle.getFigi(), null);
            strategies.parallelStream().forEach(strategy -> observeCandle(candle, strategy));
        } catch (Exception e) {
            var msg = String.format("An error during observe new candle %s, %s", candle, e.getMessage());
            log.error(msg, e);
            notificationService.sendMessage(msg);
        }
    }

    /**
     * Обработчик новой свечи по конкретной стратегии
     *
     * @param candle
     * @param strategy
     */
    private void observeCandle(CandleDomainEntity candle, AStrategy strategy) {
        // Ищем открытую позицию (ордер) по стратегии
        // - для стратегии instrumentByFiat нужен ордер по инструменту свечки (торгуется стратегия в разрезе инструмента)
        // - для стратегии instrumentByInstrument нужен ордер по любому инструменту (торгуется вся стратегия целиком)
        var figiForOrder = switch (strategy.getType()) {
            case instrumentByFiat -> candle.getFigi();
            case instrumentByInstrument -> null;
        };
        var order = orderService.findActiveByFigiAndStrategy(figiForOrder, strategy);

        // Нет активной позиции по стратегии -> возможно можем купить, иначе продать
        if (order == null) {
            buy(candle, strategy);
        } else {
            sell(candle, order, strategy);
        }
    }

    /**
     * Покупка инструмента при выполнении условий стратегии
     *
     * @param candle
     * @param strategy
     */
    private void buy(CandleDomainEntity candle, AStrategy strategy) {
        // Проверяем, включена ли покупка по стратегии (выключаем в случае завершения торговли по стратегии)
        if (strategy.isOnlySell()) {
            return;
        }

        // Рассчитываем, нужно ли покупать
        var isShouldBuy = calculator.isShouldBuy(strategy, candle);
        if (!isShouldBuy) {
            return;
        }

        // Проверяем задержку в торговле после продажи по stop loss
        if (strategy.getType() == AStrategy.Type.instrumentByFiat && strategy.getDelayBySL() != null) {
            var finishedOrders = orderService.findClosedByFigiAndStrategy(candle.getFigi(), strategy);
            if (finishedOrders.isEmpty()) {
                var lastOrder = finishedOrders.getLast();
                if (ChronoUnit.SECONDS.between(lastOrder.getSellDateTime(), candle.getDateTime()) < strategy.getDelayBySL().getSeconds()) {
                    return;
                }
            }
        }

        // Покупаем
        var order = orderService.buy(candle, strategy);
        var msg = String.format("Buy %s (%s), %s x%s, %s, %s. Wanted %s", order.getFigi(),
                order.getFigiTitle(), order.getPurchasePrice(), order.getLots(), order.getPurchaseDateTime(),
                order.getStrategy(), candle.getClosingPrice());
        log.warn(msg);
        notificationService.sendMessage(msg);
    }

    /**
     * Продажа инструмента при выполнении условий стратегии
     *
     * @param candle
     * @param order
     * @param strategy
     */
    private void sell(CandleDomainEntity candle, OrderDomainEntity order, AStrategy strategy) {
        // Позиция есть, но пришла свечка с датой до покупки текущего ордера, так не должно быть
        if (order.getPurchaseDateTime().isAfter(candle.getDateTime())) {
            log.error("Was founded order before current candle date time: {}, {}", order, candle);
            return;
        }

        // Рассчитываем, нужно ли продавать
        var isShouldSell = calculator.isShouldSell(strategy, candle, order.getPurchasePrice());
        if (!isShouldSell) {
            return;
        }

        // Продаем
        order = orderService.sell(candle, strategy);
        var msg = String.format("Sell %s (%s), %s x%s (%s), %s, %s. Wanted: %s", candle.getFigi(),
                order.getFigiTitle(), order.getSellPrice(), order.getLots(), order.getSellProfit(),
                order.getSellDateTime(), order.getStrategy(), candle.getClosingPrice());
        log.warn(msg);
        notificationService.sendMessage(msg);
    }
}
