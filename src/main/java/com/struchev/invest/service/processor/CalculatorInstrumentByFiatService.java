package com.struchev.invest.service.processor;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.service.candle.CandleHistoryService;
import com.struchev.invest.strategy.AStrategy;
import com.struchev.invest.strategy.instrument_by_fiat.AInstrumentByFiatStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Function;

/**
 * Сервис для калькуляции решения купить/продать для стратегий с типом instrumentByFiat
 * Торгует при изменении стоимости торгового инструмента относительно его валюты продажи/покупки
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CalculatorInstrumentByFiatService implements ICalculatorService<AInstrumentByFiatStrategy> {
    private final CandleHistoryService candleHistoryService;

    /**
     * Расчет перцентиля по цене инструмента за определенный промежуток
     *
     * @param candle
     * @param percentile
     * @param keyExtractor
     * @param duration
     * @return
     */
    private BigDecimal calculate(CandleDomainEntity candle, int percentile,
                                 Function<? super CandleDomainEntity, ? extends BigDecimal> keyExtractor,
                                 Duration duration) {
        return calculate(candle.getDateTime(), candle.getFigi(), percentile, keyExtractor, duration);
    }

    /**
     * Расчет перцентиля по цене инструмента за определенный промежуток
     *
     * @param currentDateTime
     * @param figi
     * @param percentile
     * @param keyExtractor
     * @param duration
     * @return
     */
    private BigDecimal calculate(OffsetDateTime currentDateTime, String figi, int percentile,
                                 Function<? super CandleDomainEntity, ? extends BigDecimal> keyExtractor,
                                 Duration duration) {
        // недостаточно данных за промежуток (первая свечка пришла позже начала интервала) - не калькулируем
        var firstCandleDateTime = candleHistoryService.getFirstCandleDateTime(figi);
        if(currentDateTime.minus(duration).isBefore(firstCandleDateTime)) {
            return null;
        }

        var startDateTime = currentDateTime.minus(duration);
        var candleHistoryLocal = candleHistoryService.getCandlesByFigiBetweenDateTimes(figi,
                startDateTime, currentDateTime);

        // недостаточно данных за промежуток (мало свечек) - не калькулируем
        if (Duration.ofMinutes(30).compareTo(duration) <= 0 && candleHistoryLocal.size() < 15) {
            return null;
        } else if (Duration.ofHours(1).compareTo(duration) <= 0 && candleHistoryLocal.size() < 30) {
            return null;
        } else if (Duration.ofHours(2).compareTo(duration) <= 0 && candleHistoryLocal.size() < 60) {
            return null;
        }

        candleHistoryLocal.sort(Comparator.comparing(keyExtractor));
        int index = candleHistoryLocal.size() * percentile / 100;
        return Optional.ofNullable(candleHistoryLocal.get(index)).map(keyExtractor).orElse(null);
    }


    /**
     * Расчет цены для покупки на основе персентиля по цене инструмента за определенный промежуток
     * Будет куплен, если текущая цена < значения персентиля
     *
     * @param strategy
     * @param candle
     * @return
     */
    @Override
    public boolean isShouldBuy(AInstrumentByFiatStrategy strategy, CandleDomainEntity candle) {
        var currentDateTime = candle.getDateTime();
        var figi = candle.getFigi();
        var buyCriteria = strategy.getBuyCriteria();
        var valueByPercentile = calculate(currentDateTime, figi, buyCriteria.getLessThenPercentile(),
                CandleDomainEntity::getLowestPrice, strategy.getHistoryDuration());

        if (valueByPercentile != null && candle.getClosingPrice().compareTo(valueByPercentile) < 0) {
            return true;
        }
        return false;
    }


    /**
     * Расчет цены для продажи на основе персентиля по цене инструмента за определенный промежуток (takeProfitPercentile, stopLossPercentile)
     * Расчет цены для продажи на основе цены покупки (takeProfitPercent, stopLossPercent)
     * Будет куплен если
     * - сработал один из takeProfitPercent, takeProfitPercentile
     * - сработали оба stopLossPercent, stopLossPercentile are happened
     *
     * @param candle
     * @param purchaseRate
     * @return
     */
    @Override
    public boolean isShouldSell(AInstrumentByFiatStrategy strategy, CandleDomainEntity candle, BigDecimal purchaseRate) {
        var sellCriteria = strategy.getSellCriteria();
        var profitPercent = candle.getClosingPrice().subtract(purchaseRate)
                .multiply(BigDecimal.valueOf(100))
                .divide(purchaseRate, 4, RoundingMode.HALF_DOWN);
        // profit % > take profit %, profit % is positive
        if (sellCriteria.getTakeProfitPercent() != null
                && profitPercent.floatValue() > sellCriteria.getTakeProfitPercent()
                && profitPercent.floatValue() > 0.1f) {
            return true;
        }

        // current price > take profit percentile, profit % is positive
        if (sellCriteria.getTakeProfitPercentile() != null) {
            var valueByPercentile = calculate(candle, sellCriteria.getTakeProfitPercentile(),
                    CandleDomainEntity::getHighestPrice, strategy.getHistoryDuration());
            if (valueByPercentile != null && candle.getClosingPrice().compareTo(valueByPercentile) > 0
                    && profitPercent.floatValue() > 0.1f) {
                return true;
            }
        }

        int countOfCriteriaToStopLoss = 0;
        int countOfSuitableToStopLoss = 0;

        // profit % < stop loss %
        if (sellCriteria.getStopLossPercent() != null) {
            countOfCriteriaToStopLoss++;
            if (profitPercent.floatValue() < -1 * sellCriteria.getStopLossPercent()) {
                countOfSuitableToStopLoss++;
            }
        }

        // current price < stop loss percentile
        if (sellCriteria.getStopLossPercentile() != null) {
            countOfCriteriaToStopLoss++;
            var valueByPercentile = calculate(candle, sellCriteria.getStopLossPercentile(),
                    CandleDomainEntity::getLowestPrice, strategy.getHistoryDuration());
            if (valueByPercentile != null && candle.getClosingPrice().compareTo(valueByPercentile) < 0) {
                countOfSuitableToStopLoss++;
            }
        }

        if (countOfCriteriaToStopLoss == 0) {
            throw new RuntimeException("No stop loss rule for " + strategy.getName());
        }
        return countOfCriteriaToStopLoss == countOfSuitableToStopLoss;
    }

    @Override
    public AStrategy.Type getStrategyType() {
        return AStrategy.Type.instrumentByFiat;
    }
}
