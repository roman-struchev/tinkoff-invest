# Торговый робот для Тинкофф Инвестиций
Разработан в рамках [Tinkoff Invest Robot Contest](https://github.com/Tinkoff/invest-robot-contest) (x-app-name `roman-struchev`)

# Конфигурация
##### Tinkoff invest API 
```properties
tinkoff.token: токен для Tinkoff GRPC API
tinkoff.is-token-sandbox: true для токена песочницы, false для боевого
tinkoff.account-id: ID счета (опционально, по умолчанию будет выбран первый счет, список счетов выводится в лог)
tinkoff.emulator: true для эмуляции ордеров, false для вызова Tinkoff GRPC API
```
##### Telegram API 
Опционально, используется для уведомлений об ордерах и ошибках
```properties
telegram.bot.token: токен телеграм бота
telegram.bot.chat-id: id чата, будет отправлен в чат, если написать боту любое сообщение
```

# Типы торговых стратегий
### 1. Покупка инструмента (ценной бумаги, валюты, ...) за другой инструмент
##### Описание
Прибыль за счет торговли при изменении стоимости торговых инструментов, относительно друг друга. В рамках одной стратегии должно быть не менее 2х инструментов.
##### Применение
Используется для перекладывания средств между валютами на московской бирже вследствие периодически меняющейся стоимости валют относительно друг друга (предположительно на фоне наличия позиций покупки/продажи конкретной валюты крупными игроками, экспортерами и т.д.) на московской бирже.

Продажа текущего инструмента и покупка другого из стратегии происходит при увеличении цены текущего относительно покупаемого на определенный процент (0.5% по умолчанию).


##### Пример работы
Стоимость USD, EUR, CNY на московской бирже за RUB. 
USD дорожает относительно RUB в течении дня, EUR при этом изменится на меньший процент или даже дешевеет, на следующий день ситуация изменится в обратную сторону и т.д.. Соответственно в первый день нужно переложить из USD в EUR, а во второй из EUR в USD, при пересчете по любой из этих валют будет прибыль.

##### Пример стратегии
2 инструмента EUR и CNY, при изменении стоимости одной из валюты на 0.5% относительно другой происходит перекладывание (есть в исходниках, запущено на продакшене)
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
##### Атрибуты стратегии 
- `AInstrumentByInstrumentStrategy.getFigies` - список FIGI (инструментов) и количество бумаг, которые используются в стратегии (минимум 2 инструмента)
- `AInstrumentByInstrumentStrategy.getMinimalDropPercent` - процент падения стоимости одного из инструментов в стратегии относительно инструмента, которым владеем (по умолчанию 0.5%). Выполняется операция продажи/покупки при достижении данного значения
##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_instrument](src/main/java/com/struchev/invest/strategy/instrument_by_instrument)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_instrument](src/test/java/com/struchev/invest/strategy/instrument_by_instrument)

### 2. Торговля инструментом за фиат (RUB, USD, EUR, ...)
##### Описание
Прибыль за счет торговли при изменении стоимости торгового инструмента. В рамках одной стратегии может быть любое кол-во инструментов.
##### Применение
Классическая покупка/продажа инструмента на основе критериев, индикаторов, истории свечей.
##### Пример стратегии
Торговля акциями Robinhood, покупаем при цене меньше 40% значения за последние 7 дней, продаем при достижении прибыли в 1%, либо убытка в 3% (есть в проекте, запущено на продакшене)
```java
@Component
public class BuyP40AndTP1PercentAndSL3PercentStrategy extends AInstrumentByFiatStrategy {

    private Map FIGIES = Map.of(
            "BBG008NMBXN8", 1    // Robinhood
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
##### Атрибуты стратегии
- `AInstrumentByFiatStrategy.getFigies` - список FIGI (инструментов) и количество бумаг, которые используются в стратегии (минимум 1 инструмент)
- `AInstrumentByFiatStrategy.getHistoryDuration` - период истории свечей, для расчета процента (перцентиля) по текущей цене относительно истории (по умолчанию 7 дней)
- `AInstrumentByFiatStrategy.getBuyCriteria().lessThenPercentile` - процент (перцентиль), если цена за указанный период падает ниже него, покупаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercent` - процент (take profit), если цена покупки растет на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercentile` - процент (take profit, перцентиль), если цена за указанный период растет выше него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercent` - процент (stop loss), если цена покупки падает на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercentile` - процент (stop loss, перцентиль), если цена за указанный период падает ниже него, продаем
- `AInstrumentByFiatStrategy.getDelayBySL` - период паузы в торговле, если продали по stop loss критерию

##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_fiat](src/main/java/com/struchev/invest/strategy/instrument_by_fiat)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_fiat](src/test/java/com/struchev/invest/strategy/instrument_by_fiat)

# Проверка стратегий по историческим данным
Приложение загружает историю, эмулирует поток свечей и ордеры в рамках прописанных стратегий. 
Работа возможна с live или sandbox токенами Tinkoff invest GRPC API.

##### Свойства
```properties
candle.history.duration: период истории свечей от времени запуска теста, используется при эмуляции потока свечей. Пример P10D (формат java.time.Duration)
```
##### Запуск через gradlew
Требуется docker, jdk 11+

1. Обновить конфигурацию (свойства) в [src/test/resources/application-test.properties](src/test/resources/application-test.properties)
2. Проверить/изменить стратегии в [src/test/java/com/struchev/invest/strategy](src/test/java/com/struchev/invest/strategy)
3. Скомпилировать и запустить тесты
```shell
./gradlew clean test --info
```
4. В консоле будет лог операций и результат.
Пример запуска теста за 30 дней и вывод результата по стратегиям типа `инструмент за инструмент`
```
---------------------- Report instrument by instrument start ----------------------
EURByCNYStrategy           | init amount 1000.00 Евро             | last amount 1104.32 Евро            | profit 10.43%  | orders 34  | commission 1099.96 RUB
EURByCNYbyUSDStrategy      | init amount 1000.00 Евро             | last amount 1166.59 Евро            | profit 16.66%  | orders 58  | commission 1836.92 RUB
USDByCNYStrategy           | init amount 6000.00 Юань             | last amount 7279.10 Юань            | profit 21.32%  | orders 55  | commission 1715.71 RUB
EURByCNYByGBPStrategy      | init amount 1000.00 Фунт стерлингов  | last amount 1376.91 Фунт стерлингов | profit 37.69%  | orders 131 | commission 4388.70 RUB
GBPByCNYStrategy           | init amount 1000.00 Фунт стерлингов  | last amount 1433.18 Фунт стерлингов | profit 43.32%  | orders 72  | commission 2502.98 RUB
JPYByCNYStrategy           | init amount 1000.00 Иена             | last amount 1651.13 Иена            | profit 65.11%  | orders 81  | commission 2398.00 RUB
JPYbyCNYByEURByGBPStrategy | init amount 1000.00 Иена             | last amount 2393.02 Иена            | profit 139.30% | orders 210 | commission 7154.09 RUB
---------------------- Report instrument by instrument end ------------------------
```

# Запуск приложения
Приложение слушает поток свечей и выполняет ордеры в рамках прописанных стратегий. 
Работа возможна с live или sandbox токенами Tinkoff invest API.
##### Используя docker-compose
Требуется docker, docker-compose
1. Обновить конфигурацию (свойства) в [docker-compose-app-with-db-local.yml](docker-compose-app-with-db-local.yml)
2. Проверить/изменить стратегии в [src/main/java/com/struchev/invest/strategy](src/main/java/com/struchev/invest/strategy)
3. Собрать docker образ локально (опционально, если меняли стратегии в проекте, иначе будет использоваться уже опубликованный из CI https://hub.docker.com/repository/docker/romanew/invest)
    ```shell
    docker build -t romanew/invest:latest -f Dockerfile.App .
    ```
4. Запустить контейнеры приложения и БД через docker-compose
    ```shell
    docker-compose -f docker-compose-app-with-db-local.yml up
    ```
5. В консоле будет лог операций, статистика доступна через UI http://localhost:10000

##### Используя gradlew (out of scope)
Требуется posgresql, jdk 11+
1. Обновить конфигурацию (свойства) и подключение к posgresql в одном из профилей. 
   Профили `application-*.properties` в [src/main/resources/](src/main/resources/).
2. Проверить/изменить стратегии в [src/main/java/com/struchev/invest/strategy](src/main/java/com/struchev/invest/strategy)
3. Скомпилировать и запустить приложение указав профиль
    ```shell
    ./gradlew bootRun -Dspring.profiles.active=sandbox
    ```
4. В консоле будет лог операций, статистика по адресу http://localhost:10000

# CI/CD
При коммите, проект собирается через github actions и docker образы публикуются в https://hub.docker.com/repository/docker/romanew/invest.
Экземпляр приложения разворачивается на VPS (http://invest.struchev.site)

На данный момент торгуются несколько стратегий, которые обгоняют рынок.
Актуальное состояние можно посмотреть на http://invest.struchev.site

# Мониторинг приложения
Подключены Spring Actuator и JavaMelody. По умолчанию открыты адреса:
- `/actuator/health`
- `/actuator/metrics`
- `/actuator/monitoring`
- `/actuator/logfile`

# Статистика по брокерскому счету (out of scope)
Для отображения детальной статистики по каждому из брокерских счетов разработан отдельный сервис http://tinkoff-pro.struchev.site