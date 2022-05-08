package com.struchev.invest.strategy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.Map;

public abstract class AStrategy {

    public abstract Map<String, Integer> getFigies();

    public final Integer getLots(String figi) {
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
        instrumentByInstrument("Инстручент за инструмент");

        @Getter
        String title;
    }

    public abstract Type getType();
}
