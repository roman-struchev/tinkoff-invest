package com.struchev.invest.service.tinkoff;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.core.InvestApi;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface ITinkoffCommonAPI {
    InvestApi getApi();
    String getAccountId();
    boolean getIsSandboxMode();
}
