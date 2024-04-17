package com.struchev.invest.service.processor;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.service.order.OrderService;
import com.struchev.invest.service.price.PricesService;
import com.struchev.invest.strategy.AStrategy;
import com.struchev.invest.strategy.instrument_by_instrument.AInstrumentByInstrumentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис для калькуляции решения купить/продать для стратегий с типом instrumentByInstrument
 * Торгует при изменении стоимости торговых инструментов, относительно друг друга (через операцию продажи и покупки)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CalculatorInstrumentByInstrumentService implements ICalculatorService<AInstrumentByInstrumentStrategy> {
    private final OrderService orderService;
    private final PricesService pricesService;

    /**
     * Будет куплен инструмент, у которого цена изменилась на меньший процент, чем у остальных из стратегии с момента последней покупки
     *
     * @param strategy
     * @param candle
     * @return
     */
    public boolean isShouldBuy(AInstrumentByInstrumentStrategy strategy, CandleDomainEntity candle) {
        // Еще нет стоимости в памяти по всем инструментам
        var currentPrices = pricesService.getCurrentPrices(strategy);
        if (!strategy.getFigies().keySet().stream().allMatch(figi -> currentPrices.get(figi) != null)) {
            return false;
        }

        // Не было ордера в рамках стратегии, то покупаем любой инструмент
        var lastOrder = orderService.findLastByFigiAndStrategy(null, strategy);
        if (lastOrder == null) {
            return true;
        }

        // Покупаем инструмент, отличающийся от последнего
        // Цена инструмента должна измениться на меньший процент, чем у остальных из стратегии с момента последней покупки
        var changePercents = lastOrder.getDetails().getCurrentPrices().entrySet().stream()
                .filter(e -> strategy.getFigies().containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    var figi = e.getKey();
                    var priceWhenBuy = e.getValue();
                    var priceCurrent = currentPrices.get(figi);
                    return (priceCurrent.doubleValue() - priceWhenBuy.doubleValue()) / priceWhenBuy.doubleValue() * 100;
                }));
        var changePercentMin = changePercents.entrySet().stream()
                .reduce((v1, v2) -> v1.getValue() < v2.getValue() ? v1 : v2).orElseThrow();
        return changePercentMin.getKey().equals(candle.getFigi());
    }

    /**
     * Будет продан купленный инструмент, если цена другого инструмента из стратегии упала на несколько % больше,
     * чем купленного в данный момент
     *
     * @param strategy
     * @param candle
     * @return
     */
    public boolean isShouldSell(AInstrumentByInstrumentStrategy strategy, CandleDomainEntity candle, BigDecimal purchasePrice) {
        // Еще нет стоимости в памяти по всем инструментам
        var currentPrices = pricesService.getCurrentPrices(strategy);
        if (!strategy.getFigies().keySet().stream().allMatch(figi -> currentPrices.get(figi) != null)) {
            return false;
        }

        // Нет купленного инструмента в рамках стратегии, нечего продавать
        var lastOpenOrder = orderService.findActiveByFigiAndStrategy(null, strategy);
        if (!lastOpenOrder.getFigi().equals(candle.getFigi())) {
            return false;
        }

        // Рассчитываем % изменения стоимости инструментов относительно последней операции покупки
        var changePercents = lastOpenOrder.getDetails().getCurrentPrices().entrySet().stream()
                .filter(e -> strategy.getFigies().containsKey(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                    var figi = e.getKey();
                    var priceWhenBuy = e.getValue();
                    var priceCurrent = currentPrices.get(figi);
                    return (priceCurrent.doubleValue() - priceWhenBuy.doubleValue()) / priceWhenBuy.doubleValue() * 100;
                }));
        var changePercentForCurrentFigi = changePercents.get(candle.getFigi());
        var changePercentMin = changePercents.entrySet().stream()
                .reduce((v1, v2) -> v1.getValue() < v2.getValue() ? v1 : v2).orElseThrow();

        // Нужно чтобы цена одного из инструментов упала (в процентах) относительно цены покупки текущего инструмента на сколько-то
        // Тогда будет выгодно продать текущую и купить другую (по рыночной цене, если будут заявки в стакане и перекроем комиссию)
        return changePercentForCurrentFigi - changePercentMin.getValue() > strategy.getMinimalDropPercent();
    }

    @Override
    public AStrategy.Type getStrategyType() {
        return AStrategy.Type.instrumentByInstrument;
    }

}
