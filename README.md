# Торговый робот для Тинкофф Инвестиций
Разработан в рамках [Tinkoff Invest Robot Contest](https://github.com/Tinkoff/invest-robot-contest) (x-app-name `roman-struchev`)

# Конфигурация
##### Tinkoff invest API 
```properties
tinkoff.token: токен для Tinkoff GRPC API
tinkoff.is-token-sandbox: true для токена песочницы, false для боевого
tinkoff.account-id: ID счета (опционально, будет выбран первый счет, если не указано)
tinkoff.emulator: true для эмуляции ордеров, false для вызова Tinkoff API
```
##### Telegram API 
Опционально, используется для уведомлений об ордерах и ошибках
```properties
telegram.bot.token: токен телеграм бота
telegram.bot.chat-id: id чата, будет отправлен в чат, если написать боту любое сообщение
```

# Типы торговых стратегий
### 1. Покупка инструмента (ценной бумаги) за другой инструмент (ценную бумагу)
##### Описание
Прибыль за счет торговли при изменении стоимости торговых инструментов, относительно друг друга. В рамках одной стратегии должно быть не менее 2х инструментов.
##### Применение
Используется для перекладывания средств между валютами на московской бирже вследствие периодически меняющейся стоимости валют относительно друг друга (предположительно на фоне наличия позиций покупки/продажи конкретной валюты крупными игроками, экспортерами и т.д.) на московской бирже.

Продажа текущего инструмента и покупка другого из стратегии происходит при увеличении цены текущего относительно покупаемого на определенный процент (0.5% по умолчанию).


##### Пример работы
Стоимость USD, EUR, CNY на московской бирже за RUB. 
USD может вырасти относительно RUB в течении дня, EUR при этом изменится на меньший процент или даже станет дешевле, на следующий день ситуация изменится в обратную сторону и т.д.. Соответственно в первый день нужно переложить из USD в EUR, а во второй из EUR в USD, при пересчете по любой из этих валют будет прибыль.

##### Пример конфигурации
Пара EUR/CNY, при изменении стоимости одной из валюты на 0.5% относительно другой происходит перекладывание (есть в исходниках, запущено на продакшене)
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
- `AInstrumentByInstrumentStrategy.getFigies` - список FIGI (инструментов) и количество бумаг, которые используются в стратегии
- `AInstrumentByInstrumentStrategy.getMinimalDropPercent` - процент падения стоимости одного из инструментов в стратегии относительно инструмента, которым владеем (по умолчанию 0.5%). Выполняется операция продажи/покупки при достижении данного значения
##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_instrument](src/main/java/com/struchev/invest/strategy/instrument_by_instrument)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_instrument](src/test/java/com/struchev/invest/strategy/instrument_by_instrument)

### 2. Торговля инструментом за фиат (RUB, USD, EUR, ...)
##### Описание
Прибыль за счет торговли при изменения стоимости торгового инструмента относительно фиатной валюты. В рамках одной стратегии может быть любое кол-во инструментов.
##### Применение
Классическая покупка/продажа инструмента на основе критериев и индикаторов.
##### Пример конфигурации
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
##### Атрибуты конфигурации:
- `AInstrumentByFiatStrategy.getFigies` - список FIGI (инструментов) и количество бумаг, которые используются в стратегии
- `AInstrumentByFiatStrategy.getHistoryDuration` - период истории котировок, для расчета процента (перцентиля) по текущей цене относительно истории (по умолчанию 7 дней)
- `AInstrumentByFiatStrategy.getBuyCriteria().lessThenPercentile` - процент (перцентиль), если цена за указанный период падает ниже него, покупаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercent` - процент (take profit), если цена покупки растет на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().takeProfitPercentile` - процент (take profit, перцентиль), если цена за указанный период растет выше него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercent` - процент (stop loss), если цена покупки падает на него, продаем
- `AInstrumentByFiatStrategy.getSellCriteria().stopLossPercentile` - процент (stop loss, перцентиль), если цена за указанный период падает ниже него, продаем
- `AInstrumentByFiatStrategy.getDelayBySL` - период паузы в торговле, если продали по stop loss критерию

##### Расположение
- live/sandbox: [src/main/java/com/struchev/invest/strategy/instrument_by_fiat](src/main/java/com/struchev/invest/strategy/instrument_by_fiat)
- tests: [src/test/java/com/struchev/invest/strategy/instrument_by_fiat](src/test/java/com/struchev/invest/strategy/instrument_by_fiat)

# Тестирование стратегий по историческим данным
Приложение загрузит историю свечей, эмулирует исторический поток данных и ордеры в рамках прописанных стратегий. 
Работа возможна с live или sandbox токенами Tinkoff invest API.

##### Свойства
```properties
candle.history.duration: период исторических свечей от текущего времени, необходимый для теста. Пример P10D (формат java.time.Duration)
```
##### Запуск через gradlew
Требуется docker, jdk 11+

1. Обновить конфигурацию (свойства) в [src/test/resources/application-test.properties](src/test/resources/application-test.properties)
2. Проверить/изменить стратегии в [src/test/java/com/struchev/invest/strategy](src/test/java/com/struchev/invest/strategy)
3. Скомпилировать и запустить тесты
```shell
./gradlew clean test --info
```
4. В консоле будет лог операций и результат

# Запуск приложения
Приложение будет слушать поток свечей и выполнять ордеры в рамках прописанных стратегий. 
Работа возможна с live или sandbox токенами Tinkoff invest API.
##### Используя docker-compose
Требуется docker и docker-compose
1. Обновить конфигурацию (свойства) в [docker-compose-app-with-db-local.yml](docker-compose-app-with-db-local.yml)
2. Проверить/изменить стратегии в [src/main/java/com/struchev/invest/strategy](src/main/java/com/struchev/invest/strategy)
3. Собрать docker образ локально (если меняли стратегии в проекте, иначе можно не собирать, т.к. будет использоваться https://hub.docker.com/repository/docker/romanew/invest)
    ```shell
    docker build -t romanew/invest:latest -f Dockerfile.App .
    ```
4. Запустить контейнеры приложения и БД через docker-compose
    ```shell
    docker-compose -f docker-compose-app-with-db-local.yml up
    ```
5. В консоле будет лог операций, статистика по адресу http://localhost:10000

##### Используя gradlew (out of scope)
Требуется posgresql, jdk 11+
1. Обновить конфигурацию (свойства) и posgresql в одном из профилей. 
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
Для отображение детальной статистики по брокерскому счету разработан отдельный сервис http://tinkoff-pro.struchev.site