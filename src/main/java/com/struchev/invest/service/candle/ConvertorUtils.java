package com.struchev.invest.service.candle;

import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ConvertorUtils {

    public static BigDecimal toBigDecimal(Quotation value, Integer scale) {
        return toBigDecimal(value, scale, null);
    }

    /**
     * @param value
     * @param scale
     * @param defaultIfZero - default value required because of executedPrice in sandbox = 0
     * @return
     */
    public static BigDecimal toBigDecimal(Quotation value, Integer scale, BigDecimal defaultIfZero) {
        if (value == null) {
            return defaultIfZero != null ? defaultIfZero : BigDecimal.ZERO;
        }
        return toBigDecimal(value.getUnits(), value.getNano(), scale, defaultIfZero);
    }

    public static BigDecimal toBigDecimal(MoneyValue value, Integer scale) {
        return toBigDecimal(value, scale, null);
    }

    /**
     * @param value
     * @param scale
     * @param defaultIfZero - default value required because of executedPrice in sandbox = 0
     * @return
     */
    public static BigDecimal toBigDecimal(MoneyValue value, Integer scale, BigDecimal defaultIfZero) {
        if (value == null) {
            return defaultIfZero != null ? defaultIfZero : BigDecimal.ZERO;
        }
        return toBigDecimal(value.getUnits(), value.getNano(), scale, defaultIfZero);
    }

    private static BigDecimal toBigDecimal(long units, int nano, Integer scale, BigDecimal defaultIfZero) {
        var result = BigDecimal.valueOf(units).add(BigDecimal.valueOf(nano, 9));
        if (scale != null) {
            result = result.setScale(scale, RoundingMode.HALF_EVEN);
        }
        if (defaultIfZero != null && result.compareTo(BigDecimal.ZERO) == 0) {
            return defaultIfZero;
        }
        return result;
    }

    static OffsetDateTime toOffsetDateTime(long seconds) {
        return Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.UTC);
    }
}
