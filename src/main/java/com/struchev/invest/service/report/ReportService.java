package com.struchev.invest.service.report;

import com.struchev.invest.dto.OrderReportInstrumentByFiatRow;
import com.struchev.invest.dto.OrderReportInstrumentByInstrumentRow;
import com.struchev.invest.dto.StrategyInfoReportRow;
import com.struchev.invest.entity.OrderDomainEntity;
import com.struchev.invest.repository.OrderRepository;
import com.struchev.invest.service.dictionary.InstrumentService;
import com.struchev.invest.strategy.AStrategy;
import com.struchev.invest.strategy.StrategySelector;
import com.struchev.invest.strategy.instrument_by_fiat.AInstrumentByFiatStrategy;
import com.struchev.invest.strategy.instrument_by_instrument.AInstrumentByInstrumentStrategy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to prepare reports by strategies and orders
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    private final OrderRepository orderRepository;
    private final EntityManager entityManager;
    private final StrategySelector strategySelector;
    private final InstrumentService instrumentService;

    private String reportInstrumentByFiatSql;

    public List<StrategyInfoReportRow> buildReportStrategiesInfo() {
        return strategySelector.getAllStrategies().stream()
                .map(s -> StrategyInfoReportRow.builder()
                        .isEnabled(s.isEnabled())
                        .name(s.getName())
                        .type(s.getType().getTitle())
                        .figies(s.getFigies().entrySet().stream().collect(Collectors.toMap(e -> instrumentService.getInstrument(e.getKey()).getName(), Map.Entry::getValue)))
                        .buyCriteria(s instanceof AInstrumentByFiatStrategy ? ((AInstrumentByFiatStrategy) s).getBuyCriteria() : null)
                        .sellCriteria(s instanceof AInstrumentByFiatStrategy ? ((AInstrumentByFiatStrategy) s).getSellCriteria() : null)
                        .history(s instanceof AInstrumentByFiatStrategy ? ((AInstrumentByFiatStrategy) s).getHistoryDuration() : null)
                        .dropPercent(s instanceof AInstrumentByInstrumentStrategy ? ((AInstrumentByInstrumentStrategy) s).getMinimalDropPercent() : null)
                        .build())
                .collect(Collectors.toList());
    }

    public List<OrderDomainEntity> getOrdersSortByIdDesc() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    // FIXME rewrite report sql to java (example was in tests) or provide JPA mapping for sql report
    // FIXME in lots should be result value, for example 1000 instead of 1 (to fix issue fix crr diff strategies)
    public List<OrderReportInstrumentByFiatRow> buildReportInstrumentByFiat() {
        var query = entityManager.createNativeQuery(reportInstrumentByFiatSql);
        return ((List<Object[]>) query.getResultList()).stream()
                .filter(r -> strategySelector.getStrategyType(String.valueOf(r[1]), null) == AStrategy.Type.instrumentByFiat)
                .map(r -> OrderReportInstrumentByFiatRow.builder()
                        .figiTitle(String.valueOf(r[0]))
                        .strategy(String.valueOf(r[1]))
                        .profitByRobot(r[2] == null ? BigDecimal.ZERO : new BigDecimal(String.valueOf(r[2])).setScale(2, RoundingMode.HALF_UP))
                        .profitByInvest(new BigDecimal(String.valueOf(r[3])))
                        .orders(Integer.valueOf(String.valueOf(r[4])))
                        .firstPrice(new BigDecimal(String.valueOf(r[5])).setScale(2, RoundingMode.HALF_UP))
                        .lastPrice(new BigDecimal(String.valueOf(r[6])).setScale(2, RoundingMode.HALF_UP))
                        .build()
                )
                .collect(Collectors.toList());
    }

    public List<OrderReportInstrumentByInstrumentRow> buildReportInstrumentByInstrument() {
        var orders = orderRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        var strategies = orders.stream().map(OrderDomainEntity::getStrategy).collect(Collectors.toSet());
        return strategies.stream()
                .filter(strategy -> strategySelector.getStrategyType(strategy, null) == AStrategy.Type.instrumentByInstrument)
                .map(strategy -> {
                    var ordersByStrategy = orders.stream()
                            .filter(o -> o.getStrategy().equals(strategy))
                            .collect(Collectors.toList());
                    var initDate = ordersByStrategy.get(0).getPurchaseDateTime();
                    var initAmount = ordersByStrategy.isEmpty() ? 0 : ordersByStrategy.get(0).getLots();
                    var initFigiTitle = ordersByStrategy.isEmpty() ? null : ordersByStrategy.get(0).getFigiTitle();
                    var lastAmount = (double) initAmount;
                    var lastAmountInInitFigi = (double) initAmount;
                    var lastDate = initDate;
                    var commission = BigDecimal.ZERO;
                    for (int i = 0; i < ordersByStrategy.size() - 1; i++) {
                        var order = ordersByStrategy.get(i);
                        var nextOrder = ordersByStrategy.get(i + 1);
                        lastAmount = lastAmount * (order.getSellPrice().doubleValue()) / nextOrder.getPurchasePrice().doubleValue();
                        if (nextOrder.getFigiTitle().equals(initFigiTitle)) {
                            // результат нужно показать в изначальном инструменте, но конвертировать нельзя, т.к. стоимость их не равнозначна
                            lastAmountInInitFigi = lastAmount;
                        }
                        lastDate = nextOrder.getSellDateTime() == null ? nextOrder.getPurchaseDateTime() : nextOrder.getSellDateTime();
                        commission = commission.add(order.getPurchaseCommission()).add(order.getSellCommission() == null ? BigDecimal.ZERO : order.getSellCommission());
                    }

                    var percent = (lastAmountInInitFigi - initAmount) / initAmount * 100;
                    return OrderReportInstrumentByInstrumentRow.builder()
                            .initDate(initDate)
                            .initAmount(new BigDecimal(initAmount).setScale(2, RoundingMode.HALF_UP))
                            .initFigiTitle(initFigiTitle)
                            .lastAmount(BigDecimal.valueOf(lastAmountInInitFigi).setScale(2, RoundingMode.HALF_UP))
                            .lastFigiTitle(initFigiTitle)
                            .strategy(strategy)
                            .orders(ordersByStrategy.size())
                            .commission(commission.setScale(2, RoundingMode.HALF_UP))
                            .percent(BigDecimal.valueOf(percent).setScale(2, RoundingMode.HALF_UP))
                            .duration(Duration.between(initDate, lastDate))
                            .build();
                })
                .sorted(Comparator.comparing(OrderReportInstrumentByInstrumentRow::getPercent))
                .collect(Collectors.toList());
    }

    public void logReportInstrumentByFiat(List<OrderReportInstrumentByFiatRow> rows) {
        log.info("---------------------- Report instrument by fiat start ----------------------");
        for (var row : rows) {
            log.info("{}: {} | init price {} | last price {} | by robot {}% | by invest: {}% | orders {}",
                    row.getStrategy(), row.getFigiTitle(), row.getFirstPrice(), row.getLastPrice(),
                    row.getProfitByRobot(), row.getProfitByInvest(), row.getOrders());
        }
        log.info("---------------------- Report instrument by fiat end ------------------------\n");
    }

    public void logReportInstrumentByInstrument(List<OrderReportInstrumentByInstrumentRow> rows) {
        log.info("---------------------- Report instrument by instrument start ----------------------");
        for (var row : rows) {
            log.info("{} | init amount {} {} | last amount {} {} | profit {}% | orders {} | commission {} RUB | {} days",
                    row.getStrategy(), row.getInitAmount(), row.getInitFigiTitle(),
                    row.getLastAmount(), row.getLastFigiTitle(), row.getPercent(), row.getOrders(),
                    row.getCommission(), row.getDuration().toDays());
        }
        log.info("---------------------- Report instrument by instrument end ------------------------\n");
    }

    @SneakyThrows
    @PostConstruct
    void init() {
        var reportInstrumentByFiatSqlResource = new DefaultResourceLoader().getResource("sql/report_instrument_by_fiat.sql");
        reportInstrumentByFiatSql = FileCopyUtils.copyToString(new InputStreamReader(reportInstrumentByFiatSqlResource.getInputStream()));
    }
}
