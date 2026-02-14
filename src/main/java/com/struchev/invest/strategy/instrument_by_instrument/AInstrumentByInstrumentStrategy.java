package com.struchev.invest.strategy.instrument_by_instrument;

import com.struchev.invest.strategy.AStrategy;

import java.time.Duration;

/**
 * Стратегии торговли, зарабатывающие на изменении стоимости торговых инструментов, относительно друг друга
 * Пример: курс доллара и курс евро на московской бирже
 */
public abstract class AInstrumentByInstrumentStrategy extends AStrategy {

    @Override
    public final Duration getDelayBySL() {
        // Delay not implemented for this type at the moment
        throw new UnsupportedOperationException("Not supported by type: " + getType());
    }

    @Override
    public final Type getType() {
        return Type.instrumentByInstrument;
    }

    /**
     * Процент падения стоимости одного из инструментов в стратегии относительно инструмента, принадлежащего нам
     * Осуществляется операция продажи/покупки при достижении этого значения
     *
     * @return
     */
    public float getMinimalDropPercent() {
        return 0.5f;
    }

    /**
     * Если стратегия зависла в инструменте (просел относительно других), то принудительно продаем через указанное время
     */
    public Duration getForceToSellDuration() {
        return Duration.ofDays(8);
    }
}
