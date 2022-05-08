package com.struchev.invest.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Map;

public abstract class AStrategy {

    /**
     * Карта FIGI: количество бумаг для торговли
     *
     * @return
     */
    public abstract Map<String, Integer> getFigies();

    /**
     * Количество бумаг для торговли заданным figi
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

    public boolean isEnabled() {
        return false;
    }

    public abstract Duration getDelayBySL();

    @AllArgsConstructor
    public enum Type {
        instrumentByFiat("Инструмент за фиат"),
        instrumentByInstrument("Инструмент за инструмент");

        @Getter
        String title;
    }

    public abstract Type getType();
}
