package com.struchev.invest.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Map;

public abstract class AStrategy {

    /**
     * Карта FIGI, количество лотов для торговли
     *
     * @return
     */
    public abstract Map<String, Integer> getFigies();

    /**
     * Количество лотов для торговли по figi
     *
     * @param figi
     * @return
     */
    public final Integer getCount(String figi) {
        return getFigies().get(figi);
    }

    public final boolean isSuitableByFigi(String figi) {
        return getFigies().containsKey(figi);
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Включена ли стратегия
     */
    public boolean isEnabled() {
        return false;
    }

    /**
     * Задержка между торговлей (продажей и покупкой) в случае продажи по stop loss
     */
    public abstract Duration getDelayBySL();

    /**
     * Разрешаем только продажу инструмента, запрещаем покупку (после продажи стратегия больше не будет торговаться)
     * Используется для завершения торговли в рамках стратегии, перед ее стратегии
     */
    public boolean isOnlySell() {
        return false;
    }

    public abstract Type getType();

    @Getter
    @AllArgsConstructor
    public enum Type {
        instrumentByFiat("Инструмент за фиат"),
        instrumentByInstrument("Инструмент за инструмент");

        private final String title;
    }
}
