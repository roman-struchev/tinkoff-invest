package com.struchev.invest.service.tinkoff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import ru.tinkoff.piapi.contract.v1.AccountType;
import ru.tinkoff.piapi.core.InvestApi;

import javax.annotation.PostConstruct;

@Slf4j
public abstract class ATinkoffAPI implements ITinkoffCommonAPI, ITinkoffOrderAPI {

    @Value("${tinkoff.token}")
    private String token;

    @Value("${tinkoff.account-id}")
    private String accountId;

    @Value("${tinkoff.is-token-sandbox:false}")
    private Boolean isSandboxMode;

    private InvestApi api;

    @Override
    public InvestApi getApi() {
        return api;
    }

    @Override
    public String getAccountId() {
        return accountId;
    }

    @Override
    public boolean getIsSandboxMode() {
        return isSandboxMode;
    }

    @PostConstruct
    private void init() {
        api = isSandboxMode ? InvestApi.createSandbox(token, "roman-struchev") : InvestApi.create(token, "roman-struchev");

        // Проверяем, что аккаунт существует (если задан в конфигах) или выбираем первый
        var accounts = isSandboxMode
                ? api.getSandboxService().getAccountsSync() : api.getUserService().getAccountsSync();
        log.info("Available accounts: {}", accounts.size());
        accounts.forEach(a -> log.info("Account id {}, name {}", a.getId(), a.getName()));
        var account = accounts.stream()
                .filter(a -> a.getType() == AccountType.ACCOUNT_TYPE_TINKOFF)
                .filter(a -> accountId == null || accountId.isEmpty() || accountId.equals(a.getId()))
                .findFirst().orElseThrow(() -> new RuntimeException("Account was not found for token " + token));
        log.info("Will use Account id {}, name {}", account.getId(), account.getName());
        accountId = account.getId();
    }
}
