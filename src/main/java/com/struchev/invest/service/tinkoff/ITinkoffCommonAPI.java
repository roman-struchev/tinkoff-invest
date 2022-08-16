package com.struchev.invest.service.tinkoff;

import ru.tinkoff.piapi.core.InvestApi;

public interface ITinkoffCommonAPI {
    InvestApi getApi();

    String getAccountId();

    boolean getIsSandboxMode();
}
