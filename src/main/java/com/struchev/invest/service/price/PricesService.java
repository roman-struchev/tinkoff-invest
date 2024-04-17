package com.struchev.invest.service.price;

import com.struchev.invest.strategy.AStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Сервис для хранения в памяти последней цены по инструменту
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PricesService {
    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>();

    public void updatePrice(String figi, BigDecimal price) {
        currentPrices.put(figi, price);
    }

    /**
     * Возвращает текущие цены инструментов, используемых в стратегии
     * На данный момент значения нужны только для стратеги с типом instrumentByInstrument
     *
     * @param strategy
     * @return
     */
    public Map<String, BigDecimal> getCurrentPrices(AStrategy strategy) {
        return switch (strategy.getType()) {
            case instrumentByInstrument:
                yield currentPrices.entrySet().stream()
                        .filter(e -> strategy.getFigies().containsKey(e.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            case instrumentByFiat:
                yield null;
        };
    }

}
