package com.struchev.invest.strategy;

import com.struchev.invest.entity.CandleDomainEntity;
import com.struchev.invest.repository.CandleRepository;
import com.struchev.invest.repository.OrderRepository;
import com.struchev.invest.service.order.OrderService;
import com.struchev.invest.service.processor.PurchaseService;
import com.struchev.invest.service.report.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;

import static org.junit.Assert.assertTrue;

@SpringBootTest
@Slf4j
@ActiveProfiles(profiles = "test")
class StrategiesByCandleHistoryTests {
    @Autowired
    CandleRepository candleRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    PurchaseService purchaseService;
    @Autowired
    OrderService orderService;
    @Autowired
    ReportService reportService;
    @Autowired
    StrategySelector strategySelector;

    @Value("${candle.history.duration}")
    private Duration historyDuration;

    @Value("${tinkoff.emulator}")
    private Boolean isTinkoffEmulator;

    @BeforeEach
    public void clean() {
        // удаляем существующие ордеры из БД
        orderRepository.deleteAll();
        orderService.loadOrdersFromDB();
    }

    @Test
    void checkProfitByStrategies() {
        // Проверяем свойства для тестов
        assertTrue("Tests are allowed with Tinkoff API emulator only (tinkoff.emulator=true)", isTinkoffEmulator);

        // Эмулируем поток свечей за заданный интервал (candle.history.duration)
        var days = historyDuration.toDays();
        var startDateTime = OffsetDateTime.now().minusDays(days);
        strategySelector.getFigiesForActiveStrategies().stream()
                .flatMap(figi -> candleRepository.findByFigiAndIntervalAndDateTimeAfterOrderByDateTime(figi, "1min", startDateTime).stream())
                .sorted(Comparator.comparing(CandleDomainEntity::getDateTime))
                .forEach(c -> purchaseService.observeNewCandle(c));

        // Логируем отчеты
        reportService.logReportInstrumentByFiat(reportService.buildReportInstrumentByFiat());
        reportService.logReportInstrumentByInstrument(reportService.buildReportInstrumentByInstrument());
        assert true;
    }

}