package com.struchev.invest.strategy.instrument_by_fiat;

import com.struchev.invest.strategy.AStrategy;
import lombok.Builder;
import lombok.Data;

import java.time.Duration;

/**
 * Стратегии торговли, зарабатывающие на изменении стоимости торговых инструментов, относительно фиатной валюты
 * Пример: продажа/покупка акций Apple за USD
 */
public abstract class AInstrumentByFiatStrategy extends AStrategy {
    /**
     * Период паузы в торговле, если продали по stop loss критерию
     * @return
     */
    @Override
    public Duration getDelayBySL() {
        return null;
    }

    /**
     * Период истории котировок, для расчета процента (перцентиля) для текущей цены относительно истории
     * @return
     */
    public Duration getHistoryDuration() {
        return Duration.ofDays(7);
    }

    @Builder
    @Data
    public static class BuyCriteria {
        // Процент (перцентиль), если цена за указанный период падает ниже него, покупаем
        Integer lessThenPercentile;
    }

    public abstract BuyCriteria getBuyCriteria();

    @Builder
    @Data
    public static class SellCriteria {
        // Процент (перцентиль), если цена за указанный период падает ниже него, продаем
        Float stopLossPercent;
        // Процент (перцентиль), если цена за указанный период падает ниже него, продаем
        Integer stopLossPercentile;
        // Процент (take profit), если цена за указанный период падает на него, продаем
        Float takeProfitPercent;
        // Процент (take profit, перцентиль), если цена за указанный период падает ниже него, продаем
        Integer takeProfitPercentile;
    }

    public abstract SellCriteria getSellCriteria();

    @Override
    public final Type getType() {
        return Type.instrumentByFiat;
    }
}
