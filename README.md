# Торговый робот для Тинкофф Инвестиций
Разработан в рамках [Tinkoff Invest Robot Contest](https://github.com/Tinkoff/invest-robot-contest)

# Конфигурация
##### Tinkoff API
```properties
tinkoff.token - токен для Tinkoff GRPC API
tinkoff.account-id - ID счета в Tinkoff (опционально, будет выбран первый счет, если не указано)
tinkoff.is-token-sandbox - true/false (по типу токена)
tinkoff.emulator - true/false (эмуляция запросов в tinkoff для выполнения ордеров)
```
##### Telegram API (опционально, уведомления о сделках и ошибках)
```properties
telegram.bot.token: токен телеграм бота
telegram.bot.chat-id: id чата, будет отправлен в чат, если написать боту любое сообщение
```

# Торговые стратегии
### 1. Покупка инструмента (ценной бумаги) за другой инструмент (ценную бумагу)
##### Описание
Стратегии торговли, зарабатывающие на изменении стоимости торговых инструментов, относительно друг друга. В рамках одной стратегии должно быть не менее 2х инструментов.
##### Применение
Используется как стратегия для перекладывания средств между валютами на московской бирже из-за периодического открытия позиций покупки/продажи конкретной валюты крупными игроками (продажа валюты экспортерами, покупка валюты ЦБ и т.д.) и отсутствия открытого рынка. 
Валюты, которые ранее не были волатильны относительно друг друга, на московской бирже стали волатильны.

Пример: Стоимость USD, EUR, CNY на московской бирже за RUB. USD может вырасти относительно RUB в течении дня, EUR при этом изменится на меньший процент или даже станет дешевле, на следующий день ситуация изменится в обратную сторону и т.д.. Соответственно в первый день нужно переложить из USD в EUR, а во второй из EUR в USD, при пересчете по любой из этих валют будет прибыль.

Продажа текущего инструмента и соотв. покупка другого из стратегии происходит при увеличении цены текущего относительно других на определенный процент (0.5% по умолчанию). Но это не значит, что данные инструменты стали дороже или дешевле относительно RUB.
##### Конфигурация стратегии
Пример стратегии: перекладывание EUR <-> USD при изменении стоимости одной из валюты на 0.5% относительно другой (есть в проекте, **запущено на продакшене**)
```java
public class EURByCNYStrategy extends AInstrumentByInstrumentStrategy {

    private Map FIGIES = Map.of(
            "BBG0013HRTL0", 6000, // CNY
            "BBG0013HJJ31", 1000 // EUR
    );

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```
##### Атрибуты конфигурации 
- `AInstrumentByInstrumentStrategy.getMinimalDropPercent` - Процент падения стоимости одного из инструментов в стратегии относительно инструмента, принадлежащего нам. Осуществляется операция продажи/покупки при достижении этого значения
##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_instrument](src/main/java/com/struchev/invest/strategy/instrument_by_instrument)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_instrument](src/test/java/com/struchev/invest/strategy/instrument_by_instrument)

### 2. Покупка инструмента (ценной бумаги) за фиат (RUB, USD, EUR, ...)
##### Описание
Стратегии торговли, зарабатывающие на изменении стоимости торгового инструмента относительно фиатной валюты. В рамках одной стратегии может быть любое кол-во инструментов.
##### Применение
Классическая покупка/продажа инструмента на основе критериев и индикаторов.
##### Конфигурация стратегии
Пример стратегии: торгуем двумя акциями Сбербанка, покупаем при цене меньше 40% значения за последние 7 дней, продаем при получении дохода в 1% (take profit) либо убытка в 3% (stop loss) (есть в проекте, **запущено на продакшене**)
```java
@Component
public class BuyP40AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private Map FIGIES = Map.of(
            "BBG004730N88", 10    // SBER
    );

    public Map<String, Integer> getFigies() {
        return FIGIES;
    }

    @Override
    public AInstrumentByFiatStrategy.BuyCriteria getBuyCriteria() {
        return AInstrumentByFiatStrategy.BuyCriteria.builder().lessThenPercentile(40).build();
    }

    @Override
    public AInstrumentByFiatStrategy.SellCriteria getSellCriteria() {
        return AInstrumentByFiatStrategy.SellCriteria.builder().takeProfitPercent(1f).stopLossPercent(3f).build();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```
##### Атрибуты конфигурации:
- `AInstrumentByFiatStrategy.getHistoryDuration` - Период истории котировок, для расчета процента (перцентиля) для текущей цены относительно истории (по умолчанию 7 дней)
- `AInstrumentByFiatStrategy.getBuyCriteria().lessThenPercentile` - Процент (перцентиль), если цена за указанный период падает ниже него, покупаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercent` - Процент (take profit), если цена покупки растет на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercentile` - Процент (take profit, перцентиль), если цена за указанный период растет выше него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercent` - Процент (stop loss), если цена покупки падает на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercentile` - Процент (stop loss, перцентиль), если цена за указанный период падает ниже него, продаем
- `AInstrumentByFiatStrategy.getDelayBySL` - Период паузы в торговле, если продали по stop loss критерию

##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_fiat](src/main/java/com/struchev/invest/strategy/instrument_by_fiat)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_fiat](src/test/java/com/struchev/invest/strategy/instrument_by_fiat)

# Тестирование стратегий по историческим данным

##### Используя gradlew
Требуется docker, jdk 11+

1. Обновить конфигурацию (свойства) в [src/test/resources/application-test.properties](src/test/resources/application-test.properties)
2. Прописать стратегии в [src/test/java/com/struchev/invest/strategy](src/test/java/com/struchev/invest/strategy)
3. Скомпилировать и запустить тесты
```shell
./gradlew clean test --info
```
4. В консоле будет лог операций и результат

# Запуск приложения (live/sandbox режим)
Стратегии находятся в [src/main/java/com/struchev/invest/strategy](src/main/java/com/struchev/invest/strategy)
##### Используя docker-compose
Требуется docker и docker-compose
1. Обновить конфигурацию (свойства) в [docker-compose-image-app-db.yml](docker-compose-app-with-db-local.yml)
2. Прописать стратегии в [src/test/java/com/struchev/invest/strategy](src/test/java/com/struchev/invest/strategy) (опционально)
3. Собрать docker образ локально (опционально, есть уже собранный https://hub.docker.com/repository/docker/romanew/invest)
    ```shell
    docker build -t romanew/invest:latest -f Dockerfile.App .
    ```
4. Запустить контейнеры приложения и БД через docker-compose
    ```shell
    docker-compose -f docker-compose-app-with-db-local.yml up
    ```
5. В консоле будет лог операций, статистика по адресу http://localhost:10000

##### Используя gradlew
Требуется posgresql, jdk 11+
1. Обновить конфигурацию (свойства) и posgresql в одном из профилей. 
   Профили `application-*.properties` в [src/main/resources/](src/main/resources/).
2. Прописать стратегии в [src/test/java/com/struchev/invest/strategy](src/test/java/com/struchev/invest/strategy) (опционально)
3. Скомпилировать и запустить приложение
    ```shell
    ./gradlew bootRun -Dspring.profiles.active=sandbox
    ```
4. В консоле будет лог операций, статистика по адресу http://localhost:10000


# CI/CD
При сборке в github docker образы публикуются в https://hub.docker.com/repository/docker/romanew/invest.
Экземпляр приложения разворачивается на сервере http://invest.struchev.site.

На данный момент торгуются несколько стратегий, подробнее можно ознакомиться на http://invest.struchev.site

# Мониторинг приложения
Подключены Spring Actuator и JavaMelody. По умолчанию доступны:
- /actuator/health
- /actuator/metrics 
- /actuator/monitoring
- /actuator/logfile