package com.struchev.invest.service.candle;

import ru.tinkoff.piapi.contract.v1.Quotation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ConvertorUtils {

    static BigDecimal toBigDecimal(Quotation value, Integer scale) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        var result = BigDecimal.valueOf(value.getUnits()).add(BigDecimal.valueOf(value.getNano(), 9));
        if (scale != null) {
            return result.setScale(scale, RoundingMode.HALF_EVEN);
        }
        return result;
    }

    static OffsetDateTime toOffsetDateTime(long seconds) {
        return Instant.ofEpochSecond(seconds).atOffset(ZoneOffset.UTC);
    }
}
